package com.example.github_workflow_tool.diffing;

import com.example.github_workflow_tool.domain.*;
import com.example.github_workflow_tool.domain.events.*;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.*;

public class DiffingServiceTests {

    private static final LocalDate referenceDate = LocalDate.of(2026, 1, 1);

    private final DiffingService sut = new DiffingService();
    private Map<Long, WorkflowRunData> before, after;

    private Instant makeInstant(int hour, int minute, int second) {
        return LocalDateTime.of(
                referenceDate,
                LocalTime.of(hour, minute, second)
        ).atZone(ZoneId.of("UTC")).toInstant();
    }

    @BeforeEach
    public void setUp() {
        JobStep step1Before = new JobStep(
                "Set up Job",
                "completed",
                "success",
                1,
                makeInstant(10, 0, 0),
                makeInstant(10, 1, 0)
        );
        JobStep step2Before = new JobStep(
                "Finish Job",
                "in_progress",
                null,
                2,
                makeInstant(10, 1, 0),
                null
        );
        JobStep step1After = new JobStep(
                "Set up Job",
                "completed",
                "success",
                1,
                makeInstant(10, 0, 0),
                makeInstant(10, 1, 0)
        );
        JobStep step2After = new JobStep(
                "Finish Job",
                "completed",
                "success",
                2,
                makeInstant(10, 1, 0),
                makeInstant(10, 2, 1)
        );
        JobStep step3After = new JobStep(
                "Set up Job",
                "completed",
                "success",
                1,
                makeInstant(10, 2, 3),
                makeInstant(10, 3, 3)
        );
        JobStep step4After = new JobStep(
                "Finish Job",
                "completed",
                "success",
                2,
                makeInstant(10, 3, 4),
                makeInstant(10, 4, 4)
        );
        JobStep step5After = new JobStep(
                "Set up Job",
                "in_progress",
                null,
                1,
                makeInstant(11, 0, 0),
                null
        );
        Job job1Before = new Job(
                10L,
                1L,
                "asdf",
                "in_progress",
                null,
                "Run tests",
                makeInstant(10, 0, 0),
                null,
                List.of(step1Before, step2Before)
        );
        Job job1After = new Job(
                10L,
                1L,
                "asdf",
                "completed",
                "success",
                "Run tests",
                makeInstant(10, 0, 0),
                makeInstant(10, 2, 1),
                List.of(step1After, step2After)
        );
        Job job2After = new Job(
                11L,
                1L,
                "asdf",
                "completed",
                "success",
                "Build executable",
                makeInstant(10, 2, 2),
                makeInstant(10, 4, 5),
                List.of(step3After, step4After)
        );
        Job job3After = new Job(
                12L,
                2L,
                "asdf",
                "in_progress",
                null,
                "Run tests",
                makeInstant(11, 0, 0),
                null,
                List.of(step5After)
        );
        WorkflowRun run1Before = new WorkflowRun(
                1L,
                100L,
                "asdf",
                "CI",
                "dev",
                "in_progress",
                null,
                makeInstant(9, 59, 0),
                makeInstant(10, 0, 0),
                makeInstant(10, 0, 0)
        );
        WorkflowRun run1After = new WorkflowRun(
                1L,
                100L,
                "asdf",
                "CI",
                "dev",
                "completed",
                "success",
                makeInstant(9, 59, 0),
                makeInstant(10, 4, 5),
                makeInstant(10, 0, 0)
        );
        WorkflowRun run2After = new WorkflowRun(
                2L,
                100L,
                "asdf",
                "CI",
                "dev",
                "in_progress",
                null,
                makeInstant(10, 59, 0),
                makeInstant(11, 0, 0),
                makeInstant(11, 0, 0)
        );
        WorkflowRunData run1DataBefore = new WorkflowRunData(run1Before, Map.of(10L, job1Before));
        WorkflowRunData run1DataAfter = new WorkflowRunData(run1After, Map.of(10L, job1After, 11L, job2After));
        WorkflowRunData run2DataAfter = new WorkflowRunData(run2After, Map.of(12L, job3After));
        before = Map.of(1L, run1DataBefore);
        after = Map.of(1L, run1DataAfter, 2L, run2DataAfter);
    }

    @Test
    public void diffDifferencesTest() {
        List<Event> eventList = List.of(
                new StepSucceededEvent(
                        makeInstant(10, 2, 1), // step2After.completedAt()
                        "dev", // run1After.headBranch()
                        "asdf", // run1After.headSha()
                        1L, // run1After.id()
                        "Finish Job", // step2After.name()
                        2, // step2After.number()
                        "Run tests" // job1After.name()
                ),
                new JobFinishedEvent(
                        makeInstant(10, 2, 1), // job1After.completedAt()
                        "dev", // run1After.headBranch()
                        "asdf", // job1After.headSha()
                        1L, // run1After.id()
                        "Run tests", // job1After.name()
                        "CI" // run1After.name()
                ),
                new JobStartedEvent(
                        makeInstant(10, 2, 2), // job2After.startedAt()
                        "dev", // run1After.headBranch()
                        "asdf", // job2After.headSha()
                        1L, // run1After.id()
                        "Build executable", // job2After.name()
                        "CI" // run1After.name()
                ),
                new StepStartedEvent(
                        makeInstant(10, 2, 3), // step3After.startedAt()
                        "dev", // run1After.headBranch()
                        "asdf", // run1After.headSha()
                        1L, // run1After.id()
                        "Set up Job", // step3After.name()
                        1, // step3After.number()
                        "Build executable" // job2After.name()
                ),
                new StepSucceededEvent(
                        makeInstant(10, 3, 3), // step3After.completedAt()
                        "dev", // run1After.headBranch()
                        "asdf", // run1After.headSha()
                        1L, // run1After.id()
                        "Set up Job", // step3After.name()
                        1, // step3After.number()
                        "Build executable" // job2After.name()
                ),
                new StepStartedEvent(
                        makeInstant(10, 3, 4), // step4After.startedAt()
                        "dev", // run1After.headBranch()
                        "asdf", // run1After.headSha()
                        1L, // run1After.id()
                        "Finish Job", // step4After.name()
                        2, // step4After.number()
                        "Build executable" // job2After.name()
                ),
                new StepSucceededEvent(
                        makeInstant(10, 4, 4), // step4After.completedAt()
                        "dev", // run1After.headBranch()
                        "asdf", // run1After.headSha()
                        1L, // run1After.id()
                        "Finish Job", // step4After.name()
                        2, // step4After.number()
                        "Build executable" // job2After.name()
                ),
                new JobFinishedEvent(
                        makeInstant(10, 4, 5), // job2After.completedAt()
                        "dev", // run1After.headBranch()
                        "asdf", // job2After.headSha()
                        1L, // run1After.id()
                        "Build executable", // job2After.name()
                        "CI" // run1After.name()
                ),
                new WorkflowQueuedEvent(
                        makeInstant(11, 0, 0), // run2After.startedAt()
                        "dev", // run2After.headBranch()
                        "asdf", // run2After.headSha()
                        2L, // run2After.id()
                        "CI" // run2After.name()
                ),
                new JobStartedEvent(
                        makeInstant(11, 0, 0), // job3After.startedAt()
                        "dev", // run2After.headBranch()
                        "asdf", // job3After.headSha()
                        2L, // run2After.id()
                        "Run tests", // job3After.name()
                        "CI" // run2After.name()
                ),
                new StepStartedEvent(
                        makeInstant(11, 0, 0), // step5After.startedAt()
                        "dev", // run2After.headBranch()
                        "asdf", // run2After.headSha()
                        2L, // run2After.id()
                        "Set up Job", // step5After.name()
                        1, // step5After.number()
                        "Run tests" // job3After.name()
                )
        );
        assertEquals(eventList, sut.computeDiff(before, after));
    }

    @Test
    public void diffNoDifferencesTest() {
        assertEquals(List.of(), sut.computeDiff(before, before));
    }
}
