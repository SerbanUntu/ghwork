package com.example.ghwork.domain.events;

import static org.fusesource.jansi.Ansi.Color;

import java.time.Instant;

/**
 * A step within a job finishing successfully.
 */
public class StepSucceededEvent extends StepEvent {

    /**
     * Constructor for a step within a job finishing successfully.
     *
     * @param timestamp  When the event occurred.
     * @param branchName The git branch relevant to the event.
     * @param commitSha  The commit SHA relevant to the event.
     * @param runId      The ID of the Actions run.
     * @param jobId      The ID of the Actions job.
     * @param stepName   The name of the Actions step.
     * @param stepNumber The number of the Actions step within the job.
     * @param jobName    The name of the Actions job.
     */
    public StepSucceededEvent(
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
     * Getter for the ANSI color for this event tag.
     *
     * @return The ANSI color for this event tag
     */
    @Override
    protected Color getColor() {
        return Color.GREEN;
    }

    /**
     * Getter for the text of the event tag.
     *
     * @return The text of the event tag for this event
     */
    @Override
    protected String getEventTag() {
        return "SUCCEEDED";
    }
}
