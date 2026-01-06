package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.domain.Job;

import java.util.*;

/**
 * The data object containing needed data from the API response from the "List jobs for a workflow run" route
 */
public record JobResponse(
        int totalCount,
        List<Job> jobs
) {
}
