package com.example.github_workflow_tool.diffing;

public enum WorkflowRunStatus {
    INITIAL(0),
    AFTER_QUEUEING(1);

    private final int order;

    public int getOrder() {
        return this.order;
    }

    WorkflowRunStatus(int order) {
        this.order = order;
    }
}
