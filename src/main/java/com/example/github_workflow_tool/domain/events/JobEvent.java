package com.example.github_workflow_tool.domain.events;

import java.time.Instant;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JobEvent jobEvent = (JobEvent) o;
        return Objects.equals(jobName, jobEvent.jobName) && Objects.equals(workflowName, jobEvent.workflowName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), jobName, workflowName);
    }
}
