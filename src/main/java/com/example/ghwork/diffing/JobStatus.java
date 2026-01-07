package com.example.ghwork.diffing;

/**
 * Maps the state of a job within a run to a discrete point in time, to aid comparison
 */
public enum JobStatus {
    INITIAL(0),
    IN_PROGRESS(1),
    COMPLETED(2);

    private final int order;

    public int getOrder() {
        return this.order;
    }

    JobStatus(int order) {
        this.order = order;
    }
}
