package com.example.ghwork.domain.events;

import java.time.Instant;

import static org.fusesource.jansi.Ansi.*;

/**
 * A job within a run starting execution
 */
public class JobStartedEvent extends JobEvent {

    public JobStartedEvent(
            Instant timestamp,
            String branchName,
            String commitSha,
            long runId,
            long jobId,
            String jobName,
            String workflowName
    ) {
        super(timestamp, branchName, commitSha, runId, jobId, jobName, workflowName);
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
        return 2;
    }

    /**
     * Getter for the ANSI color for this event tag
     *
     * @return The ANSI color for this event tag
     */
    @Override
    protected Color getColor() {
        return Color.CYAN;
    }

    /**
     * Getter for the text of the event tag
     *
     * @return The text of the event tag for this event
     */
    @Override
    protected String getEventTag() {
        return "STARTED";
    }

}
