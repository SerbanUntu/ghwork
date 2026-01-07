package com.example.github_workflow_tool.domain.events;

import java.time.Instant;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * An event concerning the lifecycle of a step within a job
 */
public abstract class StepEvent extends Event {

    private final long jobId;
    private final String stepName;
    private final int stepNumber;
    private final String jobName;

    public StepEvent(
            Instant timestamp,
            String branchName,
            String commitSha,
            long runId,
            long jobId,
            String stepName,
            int stepNumber,
            String jobName
    ) {
        super(timestamp, branchName, commitSha, runId);
        this.jobId = jobId;
        this.stepName = stepName;
        this.stepNumber = stepNumber;
        this.jobName = jobName;
    }

    /**
     * Returns the job id of an event if it has one
     *
     * @return The job id of an event, wrapped in an {@link OptionalLong}
     */
    @Override
    protected OptionalLong getJobIdForComparison() {
        return OptionalLong.of(this.jobId);
    }

    /**
     * Returns the step number of an event if it has one
     *
     * @return The step number of an event, wrapped in an {@link OptionalLong}
     */
    @Override
    protected OptionalLong getStepNumberForComparison() {
        return OptionalLong.of(this.stepNumber);
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
        return 4;
    }

    /**
     * Returns the length of the event tag for this event.
     * The tag will be padded with whitespace to the right until it matches this length.
     *
     * @return The length of the event tag for this event
     */
    @Override
    protected int getEventTagLength() {
        return 9;
    }

    /**
     * Returns the indentation of the event tag for this event.
     * The tag will be padded with whitespace to the left by this amount.
     *
     * @return The indentation of the event tag for this event
     */
    @Override
    protected int getEventTagIndentation() {
        return 2;
    }

    /**
     * Used for printing a human-friendly colored string to the terminal, using ANSI codes
     *
     * @return A human-friendly colored string representing the event data
     */
    @Override
    public String prettyPrint() {
        return appendBranchAndCommitSha(this.getEventPrefix() +
                " Step " +
                this.getPrinter().formatName(stepName) +
                " (#" +
                this.getPrinter().formatStepNumber(stepNumber) +
                ") in job " +
                this.getPrinter().formatName(jobName)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StepEvent stepEvent = (StepEvent) o;
        return stepNumber == stepEvent.stepNumber && Objects.equals(stepName, stepEvent.stepName) && Objects.equals(jobName, stepEvent.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stepName, stepNumber, jobName);
    }
}
