package com.example.ghwork.api;

import com.example.ghwork.domain.WorkflowRun;

import java.util.*;

/**
 * The data object containing needed data from the API response from the "List workflow runs for a repository" route
 */
public record WorkflowResponse(
        int totalCount,
        List<WorkflowRun> workflowRuns
) {
}
