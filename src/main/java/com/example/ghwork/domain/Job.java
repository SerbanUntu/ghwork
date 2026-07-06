package com.example.ghwork.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * The execution state of a job inside a workflow run.
 */
public record Job(
    long id,
    long runId,
    String headSha,
    String status,
    String conclusion,
    String name,
    Instant startedAt,
    Instant completedAt,
    List<JobStep> steps
) implements Serializable {
}
