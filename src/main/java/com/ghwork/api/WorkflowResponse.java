package com.ghwork.api;

import com.ghwork.domain.WorkflowRun;
import java.util.List;

/**
 * The data object containing needed data from the API response
 * from the "List workflow runs for a repository" route.
 */
public record WorkflowResponse(
    int totalCount,
    List<WorkflowRun> workflowRuns
) {

    /**
     * Constructor for a WorkflowResponse value object, used to enforce immutability.
     *
     * @param workflowRuns The execution states of all workflows returned by the API.
     */
    public WorkflowResponse {
        workflowRuns = List.copyOf(workflowRuns);
    }
}
