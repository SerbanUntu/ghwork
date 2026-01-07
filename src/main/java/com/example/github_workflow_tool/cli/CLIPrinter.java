package com.example.github_workflow_tool.cli;

import com.example.github_workflow_tool.domain.events.Event;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.fusesource.jansi.Ansi.*;

/**
 * Handles utilities for string padding and formatting for the events' data
 */
public class CLIPrinter {

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd:HH:mm:ssX";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT).withZone(ZoneId.systemDefault());
    private static final String ANSI_REGEX = "\\x1b\\[[0-9;]*[a-zA-Z]";
    private static final int COMMIT_SHA_LENGTH = 7;
    private static final int MAX_NAME_LENGTH = 13;
    private static final String TEXT_OVERFLOW = "...";
    private static final int LINE_LENGTH = 120;

    /**
     * Formats an {@link Instant} object according to the static {@code TIMESTAMP_FORMATTER}
     * @param timestamp The timestamp to format
     * @return The formatted timestamp
     */
    public String formatTimestamp(Instant timestamp) {
        return TIMESTAMP_FORMATTER.format(timestamp);
    }

    /**
     * Returns the last 3 digits of a run ID using a random color, using ANSI codes
     *
     * @param runId The ID of the run
     * @return The colored last 3 digits of the ID
     */
    public String formatRunId(long runId) {
        long hash = UUID.nameUUIDFromBytes(((Long) runId).toString().getBytes()).getMostSignificantBits();
        int color = (int) (hash % (1 << 24));
        int r = color >> 16;
        int g = (color >> 8) % 256;
        int b = color % 256;

        float average = (r + g + b) / 3.0f;
        if (average == 0.0f) {
            r = 128;
            g = 128;
            b = 128;
        } else {
            float normalizationRatio = 128 / average;
            r = Math.min((int) (r * normalizationRatio), 255);
            g = Math.min((int) (g * normalizationRatio), 255);
            b = Math.min((int) (b * normalizationRatio), 255);
        }

        return ansi()
                .a("Run ")
                .fgRgb(r, g, b)
                .a(String.format("%03d", runId % 1000))
                .fgDefault()
                .a('$')
                .reset()
                .toString();
    }

    /**
     * Pads an event tag to the right to match the length
     *
     * @param tag The event tag
     * @param length The length of the (padded) event tag
     * @return The padded tag
     */
    public String formatTag(String tag, int length) {
        return String.format("%-" + length + "s", tag == null ? "" : tag);
    }

    /**
     * Pads a job step number with zeros from the left, until it has 2 digits
     *
     * @param stepNumber The job step number to pad
     * @return The padded step number
     */
    public String formatStepNumber(int stepNumber) {
        return String.format("%02d", stepNumber);
    }

    /**
     * Truncates a commit sha so it is of a consistent length.
     * Returns question marks if a null or blank value is provided.
     *
     * @param commitSha The commit sha to format
     * @return The truncated commit sha
     */
    public String formatCommitSha(String commitSha) {
        if (commitSha == null || commitSha.isBlank()) {
            return getRepeatedString("?", COMMIT_SHA_LENGTH);
        }
        return commitSha.substring(0, COMMIT_SHA_LENGTH);
    }

    /**
     * Truncates a generic name so it has a consistent length
     * @param name The name to truncate
     * @return The truncated name, with an overflow indicator (e.g., "...")
     */
    public String formatName(String name) {
        if (name == null || name.isBlank()) return "";
        if (name.length() > MAX_NAME_LENGTH) {
            return name.substring(0, MAX_NAME_LENGTH - TEXT_OVERFLOW.length()) + TEXT_OVERFLOW;
        }
        return name;
    }

    /**
     * Calls the {@code prettyPrint()} method on a list of events, and returns the result, separated by newlines
     * @param events The list of domain events to display
     * @return The list of pretty-printed events, separated by newlines, as a {@link String}
     */
    public String prettyPrintOnSeparateLines(List<Event> events) {
        if (events == null || events.isEmpty()) return "";
        return String.join("\n",
                events.stream().map(Event::prettyPrint).toList()
        );
    }

    /**
     * Removes ANSI from an arbitrary string
     * @param ansiString A string potentially containing ANSI codes
     * @return The string without any ANSI codes
     */
    public String stripAnsi(String ansiString) {
        if (ansiString == null) return "";
        return ansiString.replaceAll(ANSI_REGEX, "");
    }

    /**
     * Repeats a pattern a number of times, to obtain a string
     * @param pattern The pattern to repeat
     * @param length The number of times to repeat the pattern
     * @return The repeated pattern, as a {@link String}
     */
    public String getRepeatedString(String pattern, int length) {
        return new String(new char[Math.max(length, 0)]).replace("\0", pattern == null ? " " : pattern);
    }

    /**
     * Calculates the padding between the event data and the branch and commit sha
     *
     * @param ansiStringBefore The event data padded to the left
     * @param branchName The branch and commit sha padded to the right
     * @return The amount of padding to add to ensure a consistent line length
     */
    public String getPaddingBeforeBranch(String ansiStringBefore, String branchName) {
        int lengthBefore = stripAnsi(ansiStringBefore).length();
        int branchNameLength = branchName == null ? 0 : branchName.length();
        int budgetLength = LINE_LENGTH -
                COMMIT_SHA_LENGTH -
                Math.min(13, branchNameLength) -
                1 -
                lengthBefore;
        return getRepeatedString(" ", Math.max(budgetLength, 1));
    }
}
