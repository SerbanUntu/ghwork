package com.example.github_workflow_tool.domain;

import java.io.Serializable;
import java.util.*;

/**
 * Mapping of a workflow run object (containing metadata about the run) to the corresponding (indexed) list of jobs.
 *
 * @param run  The workflow run object corresponding to the jobs
 * @param jobs The jobs (indexed by id) corresponding to the workflow run
 */
public record WorkflowRunData(
        WorkflowRun run,
        Map<Long, Job> jobs
) implements Serializable {
}
