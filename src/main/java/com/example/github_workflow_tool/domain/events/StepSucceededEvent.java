package com.example.github_workflow_tool.domain.events;

import java.time.Instant;

import static org.fusesource.jansi.Ansi.*;

/**
 * A step within a job finishing successfully
 */
public class StepSucceededEvent extends StepEvent {

    public StepSucceededEvent(
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
        return Color.GREEN;
    }

    /**
     * Getter for the text of the event tag
     *
     * @return The text of the event tag for this event
     */
    @Override
    protected String getEventTag() {
        return "SUCCEEDED";
    }
}
