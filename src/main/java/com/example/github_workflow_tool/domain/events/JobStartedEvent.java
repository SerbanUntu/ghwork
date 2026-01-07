package com.example.github_workflow_tool.domain.events;

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
            String jobName,
            String workflowName
    ) {
        super(timestamp, branchName, commitSha, runId, jobName, workflowName);
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
