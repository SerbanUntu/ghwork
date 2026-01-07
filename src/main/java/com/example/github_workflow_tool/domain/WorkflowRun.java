package com.example.github_workflow_tool.domain;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.Instant;

/**
 * The execution state of a GitHub workflow (excluding jobs)
 */
public record WorkflowRun(
        long id,
        long workflowId,
        String headSha,
        String name,
        String headBranch,
        String status,
        String conclusion,
        Instant createdAt,
        Instant updatedAt,
        @SerializedName("run_started_at") Instant startedAt
) implements Serializable {
}
