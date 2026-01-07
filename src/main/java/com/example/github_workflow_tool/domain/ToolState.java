package com.example.github_workflow_tool.domain;

import java.io.Serializable;
import java.util.*;

/**
 * The information stored by the tool for a specific repository
 *
 * @param runs The runs that are tracked by the tool, and compared to upstream API data
 * @param ignoredRunIds The runs that have completed and have been fully logged
 */
public record ToolState(Map<Long, WorkflowRunData> runs, Set<Long> ignoredRunIds) implements Serializable {
}
