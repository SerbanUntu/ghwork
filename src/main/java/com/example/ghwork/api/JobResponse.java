package com.example.ghwork.api;

import com.example.ghwork.domain.Job;

import java.util.*;

/**
 * The data object containing needed data from the API response from the "List jobs for a workflow run" route
 */
public record JobResponse(
        int totalCount,
        List<Job> jobs
) {
}
