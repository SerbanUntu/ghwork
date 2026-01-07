package com.example.github_workflow_tool.diffing;

/**
 * Maps a job step state to a discrete point in time, to aid comparison
 */
public enum StepStatus {
    INITIAL(0),
    IN_PROGRESS(1),
    FAILED(2),
    SUCCEEDED(2); // Both FAILED and SUCCEEDED happen at an equivalent point in time

    private final int order;

    public int getOrder() {
        return this.order;
    }

    StepStatus(int order) {
        this.order = order;
    }
}
