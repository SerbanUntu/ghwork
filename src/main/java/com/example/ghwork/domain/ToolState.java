package com.example.ghwork.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * The information stored by the tool for a specific repository.
 *
 * @param runs          The runs that are tracked by the tool, and compared to upstream API data
 * @param ignoredRunIds The runs that have completed and have been fully logged
 */
public record ToolState(Map<Long, WorkflowRunData> runs, Set<Long> ignoredRunIds)
    implements Serializable {

    /**
     * Constructor for a ToolState value object, used to enforce immutability.
     *
     * @param runs          The runs that are tracked by the tool, and compared to upstream API data
     * @param ignoredRunIds The runs that have completed and have been fully logged
     */
    public ToolState {
        runs = Map.copyOf(runs);
        ignoredRunIds = Set.copyOf(ignoredRunIds);
    }
}
