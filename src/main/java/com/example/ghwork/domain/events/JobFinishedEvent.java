package com.example.ghwork.domain.events;

import static org.fusesource.jansi.Ansi.Color;

import java.time.Instant;

/**
 * A job within a run finishing execution.
 */
public class JobFinishedEvent extends JobEvent {

    /**
     * Constructor for a job within a run finishing execution.
     *
     * @param timestamp    When the event occurred.
     * @param branchName   The git branch relevant to the event.
     * @param commitSha    The commit SHA relevant to the event.
     * @param runId        The ID of the Actions run.
     * @param jobId        The ID of the Actions job.
     * @param jobName      The name of the Actions job.
     * @param workflowName The name of the Actions workflow.
     */
    public JobFinishedEvent(
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
     * An event with a lower order is printed before an event with a higher order
     * with the same timestamp.
     *
     * @return The order of the event.
     */
    @Override
    protected int getOrder() {
        return 5;
    }

    /**
     * Getter for the ANSI color for this event tag.
     *
     * @return The ANSI color for this event tag
     */
    @Override
    protected Color getColor() {
        return Color.YELLOW;
    }

    /**
     * Getter for the text of the event tag.
     *
     * @return The text of the event tag for this event
     */
    @Override
    protected String getEventTag() {
        return "FINISHED";
    }
}
