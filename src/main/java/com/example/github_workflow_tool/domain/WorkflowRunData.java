package com.example.github_workflow_tool.domain;

import java.io.Serializable;
import java.util.*;

public record WorkflowRunData(
        WorkflowRun run,
        Map<Long, Job> jobs
) implements Serializable {
}
