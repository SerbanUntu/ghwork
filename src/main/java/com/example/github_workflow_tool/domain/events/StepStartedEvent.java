package com.example.github_workflow_tool.domain.events;

import static org.fusesource.jansi.Ansi.*;

import java.time.Instant;

/**
 * A step within a job starting execution
 */
public class StepStartedEvent extends StepEvent {

    public StepStartedEvent(
            Instant timestamp,
            String branchName,
            String commitSha,
            long runId,
            String stepName,
            int stepNumber,
            String jobName
    ) {
        super(timestamp, branchName, commitSha, runId, stepName, stepNumber, jobName);
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
        return 3;
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
