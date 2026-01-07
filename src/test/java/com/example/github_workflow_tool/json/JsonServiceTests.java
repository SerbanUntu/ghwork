package com.example.github_workflow_tool.json;

import static org.junit.jupiter.api.Assertions.*;

import com.example.github_workflow_tool.api.JobResponse;
import com.example.github_workflow_tool.api.WorkflowResponse;
import com.example.github_workflow_tool.domain.Job;
import com.example.github_workflow_tool.domain.JobStep;
import com.example.github_workflow_tool.domain.WorkflowRun;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

public class JsonServiceTests {

    private static final LocalDate referenceDate = LocalDate.of(2026, 1, 1);

    private final JsonService sut = new JsonService();

    private Instant makeInstant(int hour, int minute, int second) {
        return LocalDateTime.of(
                referenceDate,
                LocalTime.of(hour, minute, second)
        ).atZone(ZoneId.of("UTC")).toInstant();
    }

    @Test
    public void parseWorkflowResponseTest() {

        String json = """
                {
                  "total_count": 2,
                  "workflow_runs": [
                    {
                      "id": 123,
                      "workflow_id": 321,
                      "head_sha": "123abc",
                      "name": "ci",
                      "head_branch": "main",
                      "status": "completed",
                      "conclusion": "success",
                      "created_at": "2026-01-01T19:00:00Z",
                      "updated_at": "2026-01-01T19:05:00Z",
                      "run_started_at": "2026-01-01T19:05:00Z"
                    },
                    {
                      "id": 124,
                      "workflow_id": 421,
                      "head_sha": "123abc",
                      "name": "test",
                      "head_branch": "dev",
                      "status": "queued",
                      "conclusion": null,
                      "created_at": "2026-01-01T20:00:00Z",
                      "updated_at": "2026-01-01T20:05:10Z",
                      "run_started_at": null
                    }
                  ]
                }
                """;

        WorkflowRun run1 = new WorkflowRun(
                123L,
                321L,
                "123abc",
                "ci",
                "main",
                "completed",
                "success",
                makeInstant(19, 0, 0),
                makeInstant(19, 5, 0),
                makeInstant(19, 5, 0)
        );

        WorkflowRun run2 = new WorkflowRun(
                124L,
                421L,
                "123abc",
                "test",
                "dev",
                "queued",
                null,
                makeInstant(20, 0, 0),
                makeInstant(20, 5, 10),
                null
        );

        WorkflowResponse expected = new WorkflowResponse(2, List.of(run1, run2));
        assertEquals(expected, sut.parseWorkflowResponse(json));
    }

    @Test
    public void parseJobResponseTest() {

        String json = """
                {
                  "total_count": 1,
                  "jobs": [
                    {
                      "id": 123,
                      "run_id": 321,
                      "head_sha": "123abc",
                      "status": "in_progress",
                      "conclusion": null,
                      "name": "Test",
                      "started_at": "2026-01-01T10:10:10Z",
                      "completed_at": null,
                      "steps": [
                        {
                          "name": "Download source code",
                          "status": "completed",
                          "conclusion": "success",
                          "number": 1,
                          "started_at": "2026-01-01T10:10:10Z",
                          "completed_at": "2026-01-01T10:15:20Z"
                        },
                        {
                          "name": "Run tests",
                          "status": "in_progress",
                          "conclusion": null,
                          "number": 2,
                          "started_at": "2026-01-01T10:15:20Z",
                          "completed_at": null
                        }
                      ]
                    }
                  ]
                }
                """;

        JobStep step1 = new JobStep(
                "Download source code",
                "completed",
                "success",
                1,
                makeInstant(10, 10, 10),
                makeInstant(10, 15, 20)
        );

        JobStep step2 = new JobStep(
                "Run tests",
                "in_progress",
                null,
                2,
                makeInstant(10, 15, 20),
                null
        );

        Job job = new Job(
                123L,
                321L,
                "123abc",
                "in_progress",
                null,
                "Test",
                makeInstant(10, 10, 10),
                null,
                List.of(step1, step2)
        );

        JobResponse expected = new JobResponse(1, List.of(job));
        assertEquals(expected, sut.parseJobResponse(json));
    }
}
