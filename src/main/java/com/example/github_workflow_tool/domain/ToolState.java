package com.example.github_workflow_tool.domain;

import java.io.Serializable;
import java.util.*;

public record ToolState(Map<Long, WorkflowRunData> runs, Set<Long> ignoredRunIds) implements Serializable {
}
