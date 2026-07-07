package com.example.ghwork.domain;

import java.io.Serializable;
import java.util.Map;

/**
 * Mapping of a workflow run object (containing metadata about the run)
 * to the corresponding (indexed) list of jobs.
 *
 * @param run  The workflow run object corresponding to the jobs
 * @param jobs The jobs (indexed by id) corresponding to the workflow run
 */
public record WorkflowRunData(
    WorkflowRun run,
    Map<Long, Job> jobs
) implements Serializable {

    /**
     * Constructor for a WorkflowRunData value object, used to enforce immutability.
     *
     * @param run  The workflow run object corresponding to the jobs
     * @param jobs The jobs (indexed by id) corresponding to the workflow run
     */
    public WorkflowRunData {
        jobs = Map.copyOf(jobs);
    }
}
