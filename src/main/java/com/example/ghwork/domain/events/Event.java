package com.example.ghwork.domain.events;

import com.example.ghwork.cli.CLIPrinter;

import static org.fusesource.jansi.Ansi.*;

import java.time.Instant;
import java.util.*;

/**
 * A generic event concerning a GitHub workflow that is of interest to the user.
 */
public abstract class Event implements Comparable<Event> {

    private final Instant timestamp;
    private final String branchName;
    private final String commitSha;
    private final long runId;
    private final CLIPrinter printer = new CLIPrinter();

    public Event(Instant timestamp, String branchName, String commitSha, long runId) {
        this.timestamp = timestamp;
        this.branchName = branchName;
        this.commitSha = commitSha;
        this.runId = runId;
    }

    /**
     * Returns the result of a multi-condition comparison, where the
     * comparison results are in decreasing importance
     *
     * @param comparisons A list of results of {@code compareTo()} functions
     * @return The comparison result
     */
    private int getFirstNonZeroComparison(int... comparisons) {
        for (int comparison : comparisons) {
            if (comparison != 0) {
                return comparison;
            }
        }
        return comparisons[comparisons.length - 1];
    }

    /**
     * Compares two long properties of events that may or may not have a value
     *
     * @param a The first optional property
     * @param b The second optional property
     * @return Zero (equality) if one of the long values is missing, otherwise their difference
     */
    private int compareOptionalLongs(OptionalLong a, OptionalLong b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        return (int) (a.getAsLong() - b.getAsLong());
    }

    @Override
    public int compareTo(Event other) {
        return getFirstNonZeroComparison(
                this.timestamp.compareTo(other.timestamp),
                (int) (this.runId - other.runId),
                compareOptionalLongs(this.getJobIdForComparison(), other.getJobIdForComparison()),
                compareOptionalLongs(this.getStepNumberForComparison(), other.getStepNumberForComparison()),
                this.getOrder() - other.getOrder()
        );
    }

    /**
     * Returns the job id of an event if it has one
     *
     * @return The job id of an event, wrapped in an {@link OptionalLong}
     */
    protected abstract OptionalLong getJobIdForComparison();

    /**
     * Returns the step number of an event if it has one
     *
     * @return The step number of an event, wrapped in an {@link OptionalLong}
     */
    protected abstract OptionalLong getStepNumberForComparison();

    /**
     * Used for determining which event to print first,
     * when there are multiple events that happened at the same instant.
     * An event with a lower order is printed before an event with a higher order with the same timestamp.
     *
     * @return The order of the event.
     */
    protected abstract int getOrder();

    /**
     * Getter for the ANSI color for this event tag
     *
     * @return The ANSI color for this event tag
     */
    protected abstract Color getColor();

    /**
     * Getter for the text of the event tag
     *
     * @return The text of the event tag for this event
     */
    protected abstract String getEventTag();

    /**
     * Returns the length of the event tag for this event.
     * The tag will be padded with whitespace to the right until it matches this length.
     *
     * @return The length of the event tag for this event
     */
    protected abstract int getEventTagLength();

    /**
     * Returns the indentation of the event tag for this event.
     * The tag will be padded with whitespace to the left by this amount.
     *
     * @return The indentation of the event tag for this event
     */
    protected abstract int getEventTagIndentation();

    /**
     * Returns an ANSI string in the following format, shared by all events: {@code timestamp Run xxx$ TAG}
     * The "$" indicates that the digits are the last 3 digits of the full ID.
     * The run IDs are color coded, with a random color corresponding to each (full) ID.
     * The tag is an indicator such as "STARTED" or "SUCCEEDED".
     *
     * @return The ANSI prefix of an event
     */
    protected String getEventPrefix() {
        return ansi()
                .a(this.getPrinter().formatTimestamp(this.getTimestamp()))
                .a(' ')
                .a(this.getPrinter().formatRunId(this.getRunId()))
                .a(' ')
                .a(this.getPrinter().getRepeatedString(" ", this.getEventTagIndentation()))
                .fg(this.getColor())
                .a(this.getPrinter().formatTag(this.getEventTag(), this.getEventTagLength()))
                .fgDefault()
                .reset()
                .toString();
    }

    /**
     * Appends the branch name and commit sha for an event, in the format branch@commit,
     * with the necessary padding to ensure the line spans the required length precisely.
     *
     * @param eventDataAnsiString The rest of the string, not containing the branch and commit sha
     * @return The input string with the branch and commit appended
     */
    protected String appendBranchAndCommitSha(String eventDataAnsiString) {
        return eventDataAnsiString +
                this.getPrinter().getPaddingBeforeBranch(eventDataAnsiString, this.getBranchName()) +
                this.getPrinter().formatName(this.getBranchName()) +
                '@' +
                this.getPrinter().formatCommitSha(this.getCommitSha());
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public String getBranchName() {
        return this.branchName;
    }

    public String getCommitSha() {
        return this.commitSha;
    }

    public long getRunId() {
        return this.runId;
    }

    public CLIPrinter getPrinter() {
        return this.printer;
    }

    /**
     * Used for printing a human-friendly colored string to the terminal, using ANSI codes
     *
     * @return A human-friendly colored string representing the event data
     */
    public abstract String prettyPrint();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return runId == event.runId && Objects.equals(timestamp, event.timestamp) && Objects.equals(branchName, event.branchName) && Objects.equals(commitSha, event.commitSha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, branchName, commitSha, runId);
    }
}
