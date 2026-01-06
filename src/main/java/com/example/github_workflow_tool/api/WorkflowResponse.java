package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.domain.WorkflowRun;

import java.util.*;

/**
 * The data object containing needed data from the API response from the "List workflow runs for a repository" route
 */
public record WorkflowResponse(
        int totalCount,
        List<WorkflowRun> workflowRuns
) {
}
