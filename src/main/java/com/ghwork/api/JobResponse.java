package com.ghwork.api;

import com.ghwork.domain.Job;
import java.util.List;

/**
 * The data object containing needed data from the API response
 * from the "List jobs for a workflow run" route.
 */
public record JobResponse(
    int totalCount,
    List<Job> jobs
) {

    /**
     * Constructor for a JobResponse value object, used to enforce immutability.
     *
     * @param jobs The execution states of all jobs returned by the API.
     */
    public JobResponse {
        jobs = List.copyOf(jobs);
    }
}
