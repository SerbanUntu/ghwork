package com.example.github_workflow_tool.domain;

import java.time.Instant;

/**
 * The execution state of a GitHub workflow
 */
public record WorkflowRun(
        long id,
        long workflowId,
        String name,
        String headBranch,
        String status,
        String conclusion,
        Instant createdAt,
        Instant updatedAt
) {
}
