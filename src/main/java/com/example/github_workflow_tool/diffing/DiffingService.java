package com.example.github_workflow_tool.diffing;

import com.example.github_workflow_tool.domain.JobStep;
import com.example.github_workflow_tool.domain.WorkflowRunData;
import com.example.github_workflow_tool.domain.events.*;
import com.example.github_workflow_tool.domain.Job;
import com.example.github_workflow_tool.domain.WorkflowRun;

import java.util.*;

/**
 * Maps the difference in states of the workflow runs to events that happened between those states.
 * Adapter for multiple calls to a REST API, which only returns state at discrete instances of time.
 */
public class DiffingService {

    /**
     * Returns a list of all the domain events that needed to happen
     * to obtain the `afterState` from the `beforeState`
     *
     * @param beforeState The mapping of workflow runs to job runs in the current saved state.
     * @param afterState  The mapping of workflow runs to job runs at some time
     *                    after the before state, coming from fresh API data.
     *                    Must include all keys that are included in the before state.
     * @return The list of domain events that occurred between the two states, sorted by increasing timestamp.
     */
    public List<Event> computeDiff(
            Map<Long, WorkflowRunData> beforeState,
            Map<Long, WorkflowRunData> afterState
    ) {
        List<Event> events = new ArrayList<>();

        Set<Long> idsBefore = beforeState.keySet();
        for (Map.Entry<Long, WorkflowRunData> entryAfter : afterState.entrySet()) {
            Long runId = entryAfter.getKey();
            WorkflowRunData runDataAfter = entryAfter.getValue();
            WorkflowRun runAfter = runDataAfter.run();

            WorkflowRun runBefore = idsBefore.contains(runId) ? beforeState.get(runId).run() : null;
            events.addAll(compareWorkflowRuns(runBefore, runAfter));

            Map<Long, Job> jobsAfter = runDataAfter.jobs();
            Map<Long, Job> jobsBefore = runBefore == null ? (new HashMap<>()) : beforeState.get(runId).jobs();

            Set<Long> jobIdsBefore = jobsBefore.keySet();
            for (Map.Entry<Long, Job> jobEntryAfter : jobsAfter.entrySet()) {
                Long jobId = jobEntryAfter.getKey();
                Job jobAfter = jobEntryAfter.getValue();

                Job jobBefore = jobIdsBefore.contains(jobId) ? jobsBefore.get(jobId) : null;
                events.addAll(compareJobs(runAfter, jobBefore, jobAfter));

                for (int i = 0; i < jobAfter.steps().size(); i++) {
                    JobStep stepAfter = jobAfter.steps().get(i);
                    JobStep stepBefore = (jobBefore == null || jobBefore.steps().size() <= i)
                            ? null
                            : jobBefore.steps().get(i);
                    events.addAll(compareSteps(runAfter, jobAfter, stepBefore, stepAfter));
                }
            }
        }

        events.sort(Comparator.naturalOrder());
        return events;
    }

    /**
     * Maps the API state in the `status` and `conclusion` fields to a domain status used for ordering
     * @param workflowRun The run containing the fields from the API
     * @return The domain status used for ordering
     */
    private WorkflowRunStatus getWorkflowRunStatus(WorkflowRun workflowRun) {
        if (workflowRun == null) return WorkflowRunStatus.INITIAL;
        return switch (workflowRun.status()) {
            case "waiting", "requested" -> WorkflowRunStatus.INITIAL;
            default -> WorkflowRunStatus.AFTER_QUEUEING;
        };
    }

    /**
     * Maps the API state in the `status` and `conclusion` fields to a domain status used for ordering
     * @param job The job containing the fields from the API
     * @return The domain status used for ordering
     */
    private JobStatus getJobStatus(Job job) {
        if (job == null) return JobStatus.INITIAL;
        if (job.status().equals("in_progress")) return JobStatus.IN_PROGRESS;
        if (job.status().equals("completed")) return JobStatus.COMPLETED;
        return JobStatus.INITIAL;
    }

    /**
     * Maps the API state in the `status` and `conclusion` fields to a domain status used for ordering
     * @param step The job step containing the fields from the API
     * @return The domain status used for ordering
     */
    private StepStatus getStepStatus(JobStep step) {
        if (step == null) return StepStatus.INITIAL;
        if (step.status().equals("queued")) return StepStatus.INITIAL;
        if (step.status().equals("in_progress")) return StepStatus.IN_PROGRESS;
        if (step.conclusion() == null) return StepStatus.INITIAL;

        return switch (step.conclusion()) {
            case "success" -> StepStatus.SUCCEEDED;
            case "failure" -> StepStatus.FAILED;
            default -> StepStatus.INITIAL;
        };
    }

    /**
     * Diffs two run states to determine a list of events that happened to obtain the new state from the old state.
     *
     * @param runBefore The previous run state
     * @param runAfter The new run state
     * @return A list of events that occurred to obtain the new state from the old state.
     */
    private List<Event> compareWorkflowRuns(WorkflowRun runBefore, WorkflowRun runAfter) {
        List<Event> events = new ArrayList<>();
        WorkflowRunStatus statusBefore = getWorkflowRunStatus(runBefore);
        WorkflowRunStatus statusAfter = getWorkflowRunStatus(runAfter);

        if (statusBefore == WorkflowRunStatus.AFTER_QUEUEING) return events;

        if (statusAfter.getOrder() > statusBefore.getOrder()) {
            events.add(new WorkflowQueuedEvent(
                    runAfter.startedAt(),
                    runAfter.headBranch(),
                    runAfter.headSha(),
                    runAfter.id(),
                    runAfter.name()
            ));
        }

        return events;
    }

    /**
     * Diffs two job states to determine a list of events that happened to obtain the new state from the old state.
     *
     * @param run The run that the job is a part of
     * @param jobBefore The previous job state
     * @param jobAfter The new job state
     * @return A list of events that occurred to obtain the new state from the old state.
     */
    private List<Event> compareJobs(WorkflowRun run, Job jobBefore, Job jobAfter) {
        List<Event> events = new ArrayList<>();
        JobStatus statusBefore = getJobStatus(jobBefore);
        JobStatus statusAfter = getJobStatus(jobAfter);

        if (statusBefore == JobStatus.COMPLETED) return events;

        if (statusAfter.getOrder() >= JobStatus.IN_PROGRESS.getOrder() &&
                statusBefore == JobStatus.INITIAL) {
            events.add(new JobStartedEvent(
                    jobAfter.startedAt(),
                    run.headBranch(),
                    jobAfter.headSha(),
                    run.id(),
                    jobAfter.name(),
                    run.name()
            ));
        }

        if (statusBefore.getOrder() <= JobStatus.IN_PROGRESS.getOrder() &&
                statusAfter == JobStatus.COMPLETED) {
            events.add(new JobFinishedEvent(
                    jobAfter.completedAt(),
                    run.headBranch(),
                    jobAfter.headSha(),
                    run.id(),
                    jobAfter.name(),
                    run.name()
            ));
        }

        return events;
    }

    /**
     * Diffs two job step states to determine a list of events that happened to obtain the new state from the old state.
     *
     * @param run The run that the step is a part of
     * @param job The job that the step is a part of
     * @param stepBefore The previous job step state
     * @param stepAfter The new job step state
     * @return A list of events that occurred to obtain the new state from the old state.
     */
    private List<Event> compareSteps(WorkflowRun run, Job job, JobStep stepBefore, JobStep stepAfter) {
        List<Event> events = new ArrayList<>();
        StepStatus statusBefore = getStepStatus(stepBefore);
        StepStatus statusAfter = getStepStatus(stepAfter);

        if (statusBefore == StepStatus.SUCCEEDED || statusBefore == StepStatus.FAILED) return events;

        if (statusAfter.getOrder() >= StepStatus.IN_PROGRESS.getOrder() &&
                statusBefore == StepStatus.INITIAL) {
            events.add(new StepStartedEvent(
                    stepAfter.startedAt(),
                    run.headBranch(),
                    run.headSha(),
                    run.id(),
                    stepAfter.name(),
                    stepAfter.number(),
                    job.name()
            ));
        }

        if (statusBefore.getOrder() <= StepStatus.IN_PROGRESS.getOrder() &&
                statusAfter == StepStatus.FAILED) {
            events.add(new StepFailedEvent(
                    stepAfter.completedAt(),
                    run.headBranch(),
                    run.headSha(),
                    run.id(),
                    stepAfter.name(),
                    stepAfter.number(),
                    job.name()
            ));
        }

        if (statusBefore.getOrder() <= StepStatus.IN_PROGRESS.getOrder() &&
                statusAfter == StepStatus.SUCCEEDED) {
            events.add(new StepSucceededEvent(
                    stepAfter.completedAt(),
                    run.headBranch(),
                    run.headSha(),
                    run.id(),
                    stepAfter.name(),
                    stepAfter.number(),
                    job.name()
            ));
        }

        return events;
    }
}
