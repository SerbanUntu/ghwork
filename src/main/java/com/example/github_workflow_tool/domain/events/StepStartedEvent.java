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
