package com.example.ghwork.domain.events;

import static org.fusesource.jansi.Ansi.*;

import java.time.Instant;

/**
 * A step within a job finishing unsuccessfully
 */
public class StepFailedEvent extends StepEvent {

    public StepFailedEvent(
            Instant timestamp,
            String branchName,
            String commitSha,
            long runId,
            long jobId,
            String stepName,
            int stepNumber,
            String jobName
    ) {
        super(timestamp, branchName, commitSha, runId, jobId, stepName, stepNumber, jobName);
    }

    /**
     * Getter for the ANSI color for this event tag
     *
     * @return The ANSI color for this event tag
     */
    @Override
    protected Color getColor() {
        return Color.RED;
    }

    /**
     * Getter for the text of the event tag
     *
     * @return The text of the event tag for this event
     */
    @Override
    protected String getEventTag() {
        return "FAILED";
    }
}
