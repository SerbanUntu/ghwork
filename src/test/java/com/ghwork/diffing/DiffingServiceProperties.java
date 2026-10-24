package com.ghwork.diffing;

import com.ghwork.domain.Job;
import com.ghwork.domain.JobStep;
import com.ghwork.domain.WorkflowRun;
import com.ghwork.domain.WorkflowRunData;
import com.ghwork.domain.events.Event;
import com.ghwork.domain.events.JobFinishedEvent;
import com.ghwork.domain.events.JobStartedEvent;
import com.ghwork.domain.events.StepFailedEvent;
import com.ghwork.domain.events.StepStartedEvent;
import com.ghwork.domain.events.StepSucceededEvent;
import com.ghwork.domain.events.WorkflowQueuedEvent;
import net.jqwik.api.*;
import net.jqwik.time.api.DateTimes;
import net.jqwik.time.api.Times;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DiffingServiceProperties {

    private static final List<String> STEP_STATUSES = List.of("pending", "queued", "in_progress", "completed");
    private static final List<String> STEP_CONCLUSIONS = List.of("success", "failure", "skipped");
    private static final List<String> JOB_STATUSES = List.of(
            "queued", "in_progress", "completed", "waiting", "requested", "pending");
    private static final List<String> JOB_CONCLUSIONS = List.of(
            "success", "failure", "neutral", "cancelled", "skipped", "timed_out", "action_required");
    private static final List<String> RUN_STATUSES = List.of(
            "queued", "in_progress", "completed", "waiting", "requested", "pending");
    private static final List<String> RUN_CONCLUSIONS = List.of(
            "success", "failure", "neutral", "cancelled", "skipped", "timed_out", "action_required", "stale",
            "startup_failure");

    private final DiffingService sut = new DiffingService();

    // For getting around the limit of 8 arbitraries on a combine call
    private record RunIdentity(long id, long workflowId, String headSha, String name, String headBranch) {
    }

    private record Milestone(Class<? extends Event> type, List<Long> entity) {
    }

    private static UnaryOperator<String> constantly(String value) {
        return ignored -> value;
    }

    private static WorkflowRunData withJobs(WorkflowRun run, List<Job> jobs) {
        return new WorkflowRunData(run, jobs.stream()
                .map(job -> new Job(
                        job.id(),
                        run.id(),
                        run.headSha(),
                        job.status(),
                        job.conclusion(),
                        job.name(),
                        job.startedAt(),
                        job.completedAt(),
                        job.steps()
                ))
                .collect(Collectors.toMap(Job::id, Function.identity())));
    }

    private static String aggregateStatus(List<String> childStatuses) {
        if (!childStatuses.isEmpty()
                && childStatuses.stream().allMatch(status -> status != null && status.equals("completed"))) {
            return "completed";
        }
        if (childStatuses.stream().anyMatch(
                status -> status != null && (status.equals("in_progress") || status.equals("completed")))) {
            return "in_progress";
        }
        return "queued";
    }

    private static String aggregateConclusion(String status, List<String> childConclusions) {
        if (status == null || !status.equals("completed")) {
            return null;
        }
        if (childConclusions.contains("failure")) {
            return "failure";
        }
        if (childConclusions.contains("cancelled")) {
            return "cancelled";
        }
        if (!childConclusions.isEmpty() && childConclusions.stream()
                .allMatch(conclusion -> conclusion != null && conclusion.equals("skipped"))) {
            return "skipped";
        }
        return "success";
    }

    private static int statusRank(String status) {
        if (status == null) {
            return -1;
        }
        return switch (status) {
            case "requested", "waiting", "pending" -> 0;
            case "queued" -> 1;
            case "in_progress" -> 2;
            case "completed" -> 3;
            default -> -1;
        };
    }

    private static int stepStage(String status) {
        if (status == null) {
            return 0;
        }
        return switch (status) {
            case "in_progress" -> 1;
            case "completed" -> 2;
            default -> 0;
        };
    }

    private static <T> Arbitrary<List<T>> combineAll(List<Arbitrary<T>> arbitraries) {
        if (arbitraries.isEmpty()) {
            return Arbitraries.just(List.of());
        }
        return Combinators.combine(arbitraries).as(List::copyOf);
    }

    private static Job withProgress(Job job, List<JobStep> steps, Instant at) {
        boolean completed = !steps.isEmpty() && steps.stream().allMatch(step -> stepStage(step.status()) == 2);
        Instant startedAt = statusRank(job.status()) == statusRank("in_progress") && job.startedAt() != null
                ? job.startedAt()
                : at;
        Instant completedAt = completed
                ? Stream.concat(Stream.of(startedAt), steps.stream().map(JobStep::completedAt))
                        .filter(Objects::nonNull)
                        .max(Comparator.naturalOrder())
                        .orElseThrow()
                : null;
        String status = completed ? "completed" : "in_progress";
        String conclusion = aggregateConclusion(status, steps.stream().map(JobStep::conclusion).toList());
        return new Job(
                job.id(), job.runId(), job.headSha(), status, conclusion, job.name(), startedAt, completedAt, steps);
    }

    private static Set<Milestone> milestones(Map<Long, WorkflowRunData> state) {
        Set<Milestone> reached = new HashSet<>();
        for (WorkflowRunData data : state.values()) {
            long runId = data.run().id();
            if (statusRank(data.run().status()) >= statusRank("queued")) {
                reached.add(new Milestone(WorkflowQueuedEvent.class, List.of(runId)));
            }
            for (Job job : data.jobs().values()) {
                List<Long> jobEntity = List.of(runId, job.id());
                if (statusRank(job.status()) >= statusRank("in_progress")) {
                    reached.add(new Milestone(JobStartedEvent.class, jobEntity));
                }
                if (statusRank(job.status()) == statusRank("completed")) {
                    reached.add(new Milestone(JobFinishedEvent.class, jobEntity));
                }
                for (JobStep step : job.steps()) {
                    List<Long> stepEntity = List.of(runId, job.id(), (long) step.number());
                    boolean completed = stepStage(step.status()) == 2 && step.conclusion() != null;
                    boolean succeeded = completed && step.conclusion().equals("success");
                    boolean failed = completed && step.conclusion().equals("failure");
                    if (stepStage(step.status()) == 1 || succeeded || failed) {
                        reached.add(new Milestone(StepStartedEvent.class, stepEntity));
                    }
                    if (succeeded) {
                        reached.add(new Milestone(StepSucceededEvent.class, stepEntity));
                    }
                    if (failed) {
                        reached.add(new Milestone(StepFailedEvent.class, stepEntity));
                    }
                }
            }
        }
        return reached;
    }

    private static Set<List<Long>> entities(Map<Long, WorkflowRunData> state) {
        Set<List<Long>> entities = new HashSet<>();
        state.forEach((runId, data) -> {
            entities.add(List.of(runId));
            data.jobs().forEach((jobId, job) -> {
                entities.add(List.of(runId, jobId));
                job.steps().forEach(step -> entities.add(List.of(runId, jobId, (long) step.number())));
            });
        });
        return entities;
    }

    private static OptionalLong invokeGetter(Event event, String getterName) {
        try {
            Method getter = Event.class.getDeclaredMethod(getterName);
            getter.setAccessible(true);
            return (OptionalLong) getter.invoke(event);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Long> entitiesOf(Event event) {
        return Stream.of(
                        OptionalLong.of(event.getRunId()),
                        invokeGetter(event, "getJobIdForComparison"),
                        invokeGetter(event, "getStepNumberForComparison"))
                .flatMapToLong(OptionalLong::stream)
                .boxed()
                .toList();
    }

    Arbitrary<Duration> positiveDurations() {
        return Times.durations()
                .between(Duration.ZERO, Duration.ofDays(30))
                .ofPrecision(ChronoUnit.MILLIS);
    }

    Arbitrary<Duration> negativeDurations() {
        return Times.durations()
                .between(Duration.ofDays(-30), Duration.ofMillis(-1))
                .ofPrecision(ChronoUnit.MILLIS);
    }

    Arbitrary<Duration> mostlyPositiveDurations() {
        return Arbitraries.frequencyOf(
                Tuple.of(99, positiveDurations()),
                Tuple.of(1, negativeDurations())
        );
    }

    Arbitrary<Instant> instants() {
        return DateTimes.instants().ofPrecision(ChronoUnit.MILLIS);
    }

    Arbitrary<String> texts() {
        return Arbitraries.strings().ofMaxLength(20);
    }

    Arbitrary<String> shas() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .withCharRange('a', 'f')
                .ofLength(40);
    }

    Arbitrary<UnaryOperator<String>> deviationsFrom(List<String> wellDefinedValues) {
        return Arbitraries.frequencyOf(
                Tuple.of(95, Arbitraries.just(UnaryOperator.identity())),
                Tuple.of(4, Arbitraries.of(wellDefinedValues).map(DiffingServiceProperties::constantly)),
                Tuple.of(1, texts().injectNull(0.5).map(DiffingServiceProperties::constantly))
        );
    }

    Arbitrary<JobStep> jobSteps() {
        Arbitrary<String> names = texts().injectNull(0.01);
        Arbitrary<String> statuses = Arbitraries.frequencyOf(
                Tuple.of(95, Arbitraries.of(STEP_STATUSES)),
                Tuple.of(4, texts()),
                Tuple.of(1, Arbitraries.just(null))
        );
        Arbitrary<String> conclusions = Arbitraries.frequencyOf(
                Tuple.of(55, Arbitraries.of(STEP_CONCLUSIONS)),
                Tuple.of(40, Arbitraries.just(null)),
                Tuple.of(5, texts())
        );
        Arbitrary<Instant> startTimes = instants();
        Arbitrary<Duration> deltas = mostlyPositiveDurations();
        return Combinators.combine(names, statuses, conclusions, startTimes, deltas)
            .as((name, status, conclusion, startTime, delta) ->
                new JobStep(
                    name,
                    status,
                    conclusion,
                    1,
                    startTime,
                    (startTime == null || delta == null) ? null : startTime.plus(delta)));
    }

    Arbitrary<Job> jobs() {
        Arbitrary<Long> ids = Arbitraries.longs();
        Arbitrary<UnaryOperator<String>> statusDeviations = deviationsFrom(JOB_STATUSES);
        Arbitrary<UnaryOperator<String>> conclusionDeviations = deviationsFrom(JOB_CONCLUSIONS);
        Arbitrary<String> names = texts().injectNull(0.01);
        Arbitrary<Duration> setupDeltas = mostlyPositiveDurations();
        Arbitrary<Duration> tearDownDeltas = mostlyPositiveDurations();
        Arbitrary<List<JobStep>> jobStepLists = jobSteps().list().ofMaxSize(10);
        Arbitrary<Instant> fallbackTimes = instants();
        return Combinators.combine(
                ids, statusDeviations, conclusionDeviations, names, setupDeltas, tearDownDeltas, jobStepLists,
                fallbackTimes
        ).as((id, statusDeviation, conclusionDeviation, name, setupDelta, tearDownDelta, steps, fallbackTime) -> {
            List<JobStep> numberedSteps = IntStream.range(0, steps.size())
                    .mapToObj(i -> new JobStep(
                            steps.get(i).name(),
                            steps.get(i).status(),
                            steps.get(i).conclusion(),
                            i + 1,
                            steps.get(i).startedAt(),
                            steps.get(i).completedAt()
                    ))
                    .toList();
            String derivedStatus = aggregateStatus(steps.stream().map(JobStep::status).toList());
            String derivedConclusion = aggregateConclusion(
                    derivedStatus, steps.stream().map(JobStep::conclusion).toList());
            Instant earliestStepStart = steps.stream()
                    .map(JobStep::startedAt)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(fallbackTime);
            Instant latestStepEnd = steps.stream()
                    .map(JobStep::completedAt)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(fallbackTime);
            return new Job(
                    id,
                    1L,
                    "",
                    statusDeviation.apply(derivedStatus),
                    conclusionDeviation.apply(derivedConclusion),
                    name,
                    earliestStepStart.minus(setupDelta),
                    latestStepEnd.plus(tearDownDelta),
                    numberedSteps
            );
        });
    }

    Arbitrary<RunIdentity> runIdentities() {
        Arbitrary<Long> ids = Arbitraries.longs();
        Arbitrary<Long> workflowIds = Arbitraries.longs();
        Arbitrary<String> headShas = Arbitraries.frequencyOf(
                Tuple.of(95, shas()),
                Tuple.of(4, texts()),
                Tuple.of(1, Arbitraries.just(null))
        );
        Arbitrary<String> names = texts().injectNull(0.01);
        Arbitrary<String> headBranches = texts().injectNull(0.01);
        return Combinators.combine(ids, workflowIds, headShas, names, headBranches).as(RunIdentity::new);
    }

    Arbitrary<WorkflowRunData> workflowRunData() {
        Arbitrary<RunIdentity> identities = runIdentities();
        Arbitrary<UnaryOperator<String>> statusDeviations = deviationsFrom(RUN_STATUSES);
        Arbitrary<UnaryOperator<String>> conclusionDeviations = deviationsFrom(RUN_CONCLUSIONS);
        Arbitrary<Duration> creationDeltas = mostlyPositiveDurations();
        Arbitrary<Duration> updateDeltas = mostlyPositiveDurations();
        Arbitrary<Duration> setupDeltas = mostlyPositiveDurations();
        Arbitrary<List<Job>> jobLists = jobs().list().ofMaxSize(5).uniqueElements(Job::id);
        Arbitrary<Instant> fallbackTimes = instants();
        return Combinators.combine(
                identities, statusDeviations, conclusionDeviations, creationDeltas, updateDeltas, setupDeltas,
                jobLists, fallbackTimes
        ).as((identity, statusDeviation, conclusionDeviation, creationDelta, updateDelta, setupDelta, jobs,
              fallbackTime) -> {
            String derivedStatus = aggregateStatus(jobs.stream().map(Job::status).toList());
            String derivedConclusion = aggregateConclusion(
                    derivedStatus, jobs.stream().map(Job::conclusion).toList());
            Instant earliestJobStart = jobs.stream()
                    .map(Job::startedAt)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(fallbackTime);
            Instant startedAt = earliestJobStart.minus(setupDelta);
            Instant createdAt = startedAt.minus(creationDelta);
            Instant updatedAt = createdAt.plus(creationDelta).plus(updateDelta);
            WorkflowRun run = new WorkflowRun(
                    identity.id(),
                    identity.workflowId(),
                    identity.headSha(),
                    identity.name(),
                    identity.headBranch(),
                    statusDeviation.apply(derivedStatus),
                    conclusionDeviation.apply(derivedConclusion),
                    createdAt,
                    updatedAt,
                    startedAt
            );
            return withJobs(run, jobs);
        });
    }

    <T> Arbitrary<List<T>> keepAdvanceOrDrop(List<T> values, Function<T, Arbitrary<T>> advance) {
        return combineAll(values.stream()
                .map(value -> Arbitraries.<Optional<T>>frequencyOf(
                        Tuple.of(30, Arbitraries.just(Optional.of(value))),
                        Tuple.of(65, advance.apply(value).map(Optional::of)),
                        Tuple.of(5, Arbitraries.just(Optional.empty()))
                ))
                .toList()
        ).map(changed -> changed.stream().flatMap(Optional::stream).toList());
    }

    Arbitrary<JobStep> advanceStep(JobStep step, Instant now) {
        int stage = stepStage(step.status());
        if (stage == 2) {
            return Arbitraries.just(step);
        }
        return Combinators.combine(
                Arbitraries.integers().between(1, 2), Arbitraries.of(STEP_CONCLUSIONS), positiveDurations(),
                positiveDurations()
        ).as((amount, conclusion, startDelay, runTime) -> {
            Instant startedAt = stage == 1 && step.startedAt() != null ? step.startedAt() : now.plus(startDelay);
            if (stage + amount == 1) {
                return new JobStep(step.name(), "in_progress", null, step.number(), startedAt, null);
            }
            String outcome = stage == 1 && conclusion.equals("skipped") ? "success" : conclusion;
            Instant completedAt = (startedAt.isAfter(now) ? startedAt : now).plus(runTime);
            return new JobStep(step.name(), "completed", outcome, step.number(), startedAt, completedAt);
        });
    }

    Arbitrary<Job> advanceJob(Job job, Instant now) {
        if (statusRank(job.status()) == statusRank("completed")) {
            return Arbitraries.just(job);
        }
        Arbitrary<List<JobStep>> steps = job.steps().isEmpty() && statusRank(job.status()) != statusRank("in_progress")
                ? Arbitraries.integers().between(1, 3).list().ofMinSize(1).ofMaxSize(8).map(gaps -> {
                    List<JobStep> pendingSteps = new ArrayList<>();
                    int number = 0;
                    for (int gap : gaps) {
                        number += gap;
                        pendingSteps.add(new JobStep("Step " + number, "pending", null, number, null, null));
                    }
                    return pendingSteps;
                })
                : keepAdvanceOrDrop(job.steps(), step -> advanceStep(step, now));
        return Combinators.combine(steps, positiveDurations())
                .as((advancedSteps, delay) -> withProgress(job, advancedSteps, now.plus(delay)));
    }

    Arbitrary<WorkflowRunData> advanceRun(WorkflowRunData data, Instant now) {
        WorkflowRun run = data.run();
        if (statusRank(run.status()) == statusRank("completed")) {
            return Arbitraries.just(data);
        }
        List<Job> jobs = data.jobs().values().stream().sorted(Comparator.comparingLong(Job::id)).toList();
        Arbitrary<List<Job>> newJobs = Combinators.combine(
                Arbitraries.longs().filter(id -> !data.jobs().containsKey(id)), texts(), positiveDurations()
        ).as((id, name, delay) -> new Job(id, 0L, null, "queued", null, name, now.plus(delay), null, List.of()))
                .list()
                .ofMaxSize(3)
                .uniqueElements(Job::id);
        return Combinators.combine(
                keepAdvanceOrDrop(jobs, job -> advanceJob(job, now)), newJobs, positiveDurations()
        ).as((changed, added, delay) -> {
            List<Job> all = Stream.concat(changed.stream(), added.stream()).toList();
            String status = all.isEmpty() ? "queued" : aggregateStatus(all.stream().map(Job::status).toList());
            String conclusion = aggregateConclusion(status, all.stream().map(Job::conclusion).toList());
            WorkflowRun advanced = new WorkflowRun(
                    run.id(), run.workflowId(), run.headSha(), run.name(), run.headBranch(), status, conclusion,
                    run.createdAt(), now.plus(delay), run.startedAt());
            return withJobs(advanced, all);
        });
    }

    Arbitrary<Map<Long, WorkflowRunData>> progressionFrom(Map<Long, WorkflowRunData> before) {
        Instant now = before.values().stream()
                .flatMap(data -> Stream.concat(
                        Stream.of(data.run().createdAt(), data.run().updatedAt(), data.run().startedAt()),
                        data.jobs().values().stream().flatMap(job -> Stream.concat(
                                Stream.of(job.startedAt(), job.completedAt()),
                                job.steps().stream().flatMap(step -> Stream.of(step.startedAt(), step.completedAt()))))))
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(Instant.EPOCH);
        List<Arbitrary<WorkflowRunData>> runs = before.values().stream()
                .sorted(Comparator.comparingLong(data -> data.run().id()))
                .map(data -> Arbitraries.frequencyOf(
                        Tuple.of(1, Arbitraries.just(data)),
                        Tuple.of(2, advanceRun(data, now))
                ))
                .toList();
        Arbitrary<List<WorkflowRunData>> newRuns = Combinators.combine(
                runIdentities().filter(identity -> !before.containsKey(identity.id())),
                Arbitraries.of("requested", "waiting", "pending", "queued"),
                positiveDurations()
        ).as((identity, status, delay) -> new WorkflowRunData(new WorkflowRun(
                identity.id(), identity.workflowId(), identity.headSha(), identity.name(), identity.headBranch(),
                status, null, now.plus(delay), now.plus(delay), now.plus(delay)), Map.of())
        ).list().ofMaxSize(3).uniqueElements(data -> data.run().id());
        return Combinators.combine(combineAll(runs), newRuns).as((changed, added) -> Stream
                .concat(changed.stream(), added.stream())
                .collect(Collectors.toMap(data -> data.run().id(), Function.identity()))
        ).withoutEdgeCases();
    }

    Arbitrary<Tuple.Tuple2<Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>>> progressionOf(
            Arbitrary<Map<Long, WorkflowRunData>> states) {
        return states.flatMap(before -> progressionFrom(before).map(after -> Tuple.of(before, after)));
    }

    @Provide
    Arbitrary<Map<Long, WorkflowRunData>> workflowMaps() {
        return workflowRunData()
                .list()
                .ofMaxSize(5)
                .uniqueElements(wrd -> wrd.run().id())
                .map(runs -> runs.stream().collect(Collectors.toMap(wrd -> wrd.run().id(), Function.identity())));
    }

    @Provide
    Arbitrary<Tuple.Tuple2<Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>>> shuffledWorkflowMaps() {
        return workflowMaps().flatMap(state -> Arbitraries.shuffle(List.copyOf(state.values())).map(runs -> {
            Map<Long, WorkflowRunData> shuffled = new LinkedHashMap<>();
            runs.forEach(data -> shuffled.put(data.run().id(), data));
            return Tuple.of(state, shuffled);
        }));
    }

    @Provide
    Arbitrary<Tuple.Tuple2<Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>>> stateProgressions2() {
        return progressionOf(workflowMaps());
    }

    @Provide
    Arbitrary<Tuple.Tuple3<Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>>> stateProgressions3() {
        return stateProgressions2().flatMap(states -> progressionFrom(states.get2())
                .map(third -> Tuple.of(states.get1(), states.get2(), third)));
    }

    @Property
    boolean noEventsProducedForUnchangedState(@ForAll("workflowMaps") Map<Long, WorkflowRunData> workflowMaps) {
        return sut.computeDiff(workflowMaps, workflowMaps).isEmpty();
    }

    @Property
    boolean inputOrderDoesNotMatter(@ForAll("shuffledWorkflowMaps") Tuple.Tuple2<Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>> workflowMaps) {
        return sut.computeDiff(workflowMaps.get1(), workflowMaps.get2()).isEmpty();
    }

    @Property
    boolean eventsAreSortedByTimestampAndType(@ForAll("stateProgressions2") Tuple.Tuple2<Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>> stateProgressions) {
        var events = sut.computeDiff(stateProgressions.get1(), stateProgressions.get2());
        for (int i = 1; i < events.size(); i++) {
            if (events.get(i - 1).getTimestamp().isAfter(events.get(i).getTimestamp())) {
                return false;
            }
            if (events.get(i - 1).getTimestamp().equals(events.get(i).getTimestamp())
                    && events.get(i - 1).compareTo(events.get(i)) > 0) {
                return false;
            }
        }
        return true;
    }

    @Property
    boolean eventsCorrespondToTransitions(@ForAll("stateProgressions2") Tuple.Tuple2<Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>> stateProgressions) {
        Set<Milestone> expectedMilestoneSet = new HashSet<>(milestones(stateProgressions.get2()));
        expectedMilestoneSet.removeAll(milestones(stateProgressions.get1()));

        List<Milestone> actualMilestones = sut.computeDiff(stateProgressions.get1(), stateProgressions.get2()).stream()
                .map(event -> new Milestone(event.getClass(), entitiesOf(event)))
                .toList();
        Set<Milestone> actualMilestoneSet = new HashSet<>(actualMilestones);

        return actualMilestones.size() == actualMilestoneSet.size() && actualMilestoneSet.equals(expectedMilestoneSet);
    }

    @Property
    boolean stateDiffsAreTransitive(@ForAll("stateProgressions3") Tuple.Tuple3<Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>> stateProgressions) {
        var byParts = sut.computeDiff(stateProgressions.get1(), stateProgressions.get2());
        byParts.addAll(sut.computeDiff(stateProgressions.get2(), stateProgressions.get3()));
        Set<List<Long>> visibleEntities = entities(stateProgressions.get3());
        byParts.removeIf(event -> !visibleEntities.contains(entitiesOf(event)));
        byParts.sort(Comparator.naturalOrder());

        var allEvents = sut.computeDiff(stateProgressions.get1(), stateProgressions.get3());
        return byParts.equals(allEvents);
    }

    @Property
    boolean noDuplicatesAreEmitted(@ForAll("stateProgressions3") Tuple.Tuple3<Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>, Map<Long, WorkflowRunData>> stateProgressions) {
        var firstEvents  = sut.computeDiff(stateProgressions.get1(), stateProgressions.get2());
        var secondEvents = sut.computeDiff(stateProgressions.get2(), stateProgressions.get3());

        return Collections.disjoint(firstEvents, secondEvents);
    }
}
