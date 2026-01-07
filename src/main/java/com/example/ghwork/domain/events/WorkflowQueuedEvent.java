package com.example.ghwork.domain.events;

import java.time.Instant;
import java.util.Objects;
import java.util.OptionalLong;

import static org.fusesource.jansi.Ansi.*;

/**
 * A workflow run being queued for execution on a server
 */
public class WorkflowQueuedEvent extends Event {

    private final String workflowName;

    public WorkflowQueuedEvent(
            Instant timestamp,
            String branchName,
            String commitSha,
            long runId,
            String workflowName
    ) {
        super(timestamp, branchName, commitSha, runId);
        this.workflowName = workflowName;
    }

    /**
     * Returns the job id of an event if it has one
     *
     * @return The job id of an event, wrapped in an {@link OptionalLong}
     */
    @Override
    protected OptionalLong getJobIdForComparison() {
        return OptionalLong.empty();
    }

    /**
     * Returns the step number of an event if it has one
     *
     * @return The step number of an event, wrapped in an {@link OptionalLong}
     */
    @Override
    protected OptionalLong getStepNumberForComparison() {
        return OptionalLong.empty();
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
        return 1;
    }

    /**
     * Getter for the ANSI color for this event tag
     *
     * @return The ANSI color for this event tag
     */
    @Override
    protected Color getColor() {
        return Color.WHITE;
    }

    /**
     * Getter for the text of the event tag
     *
     * @return The text of the event tag for this event
     */
    @Override
    protected String getEventTag() {
        return "QUEUED";
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
                " Workflow " +
                this.getPrinter().formatName(this.workflowName)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkflowQueuedEvent that = (WorkflowQueuedEvent) o;
        return Objects.equals(workflowName, that.workflowName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), workflowName);
    }
}
