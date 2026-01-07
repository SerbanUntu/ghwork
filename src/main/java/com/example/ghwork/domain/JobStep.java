package com.example.ghwork.domain;

import java.io.Serializable;
import java.time.Instant;

/**
 * The execution state of a step inside a job
 */
public record JobStep(
        String name,
        String status,
        String conclusion,
        int number,
        Instant startedAt,
        Instant completedAt
) implements Serializable {
}
