package com.example.github_workflow_tool.domain.events;

import java.time.Instant;

/**
 * An event concerning the lifecycle of a job within a run
 */
public abstract class JobEvent extends Event {

    private final String jobName;
    private final String workflowName;

    public JobEvent(
            Instant timestamp,
            String branchName,
            String commitSha,
            long runId,
            String jobName,
            String workflowName
    ) {
        super(timestamp, branchName, commitSha, runId);
        this.jobName = jobName;
        this.workflowName = workflowName;
    }

    /**
     * Used for determining which event to print first,
     * when there are multiple events that happened at the same instant.
     * An event with a lower order is printed before an event with a higher order with the same timestamp.
     *
     * @return The order of the event.
     */
    @Override
    protected int getOrder() {
        return 5;
    }

    /**
     * Returns the length of the event tag for this event.
     * The tag will be padded with whitespace to the right until it matches this length.
     *
     * @return The length of the event tag for this event
     */
    @Override
    protected int getEventTagLength() {
        return 8;
    }

    /**
     * Returns the indentation of the event tag for this event.
     * The tag will be padded with whitespace to the left by this amount.
     *
     * @return The indentation of the event tag for this event
     */
    @Override
    protected int getEventTagIndentation() {
        return 0;
    }

    /**
     * Used for printing a human-friendly colored string to the terminal, using ANSI codes
     *
     * @return A human-friendly colored string representing the event data
     */
    @Override
    public String prettyPrint() {
        return appendBranchAndCommitSha(this.getEventPrefix() +
                " Job " +
                this.getPrinter().formatName(jobName) +
                " in workflow " +
                this.getPrinter().formatName(workflowName)
        );
    }
}
