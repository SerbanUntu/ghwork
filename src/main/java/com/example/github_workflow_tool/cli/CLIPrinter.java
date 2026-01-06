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
    private static final String ANSI_REGEX = "\\x1b\\[[0-9;]*[a-zA-Z]";
    private static final int COMMIT_SHA_LENGTH = 7;
    private static final int MAX_NAME_LENGTH = 13;
    private static final String TEXT_OVERFLOW = "...";
    private static final int LINE_LENGTH = 120;

    //map run to id and vice-versa

    public String formatTimestamp(Instant timestamp) {
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT)
                .withZone(ZoneId.systemDefault());
        return timestampFormatter.format(timestamp);
    }

    public String formatRunId(long runId) {
        long hash = UUID.nameUUIDFromBytes(((Long) runId).toString().getBytes()).getMostSignificantBits();
        int color = (int) (hash % (1 << 24));
        int r = color >> 16;
        int g = (color >> 8) % 256;
        int b = color % 256;

        float average = (r + g + b) / 3.0f;
        float normalizationRatio = 128 / average;
        r = Math.min((int) (r * normalizationRatio), 255);
        g = Math.min((int) (g * normalizationRatio), 255);
        b = Math.min((int) (b * normalizationRatio), 255);

        return ansi()
                .a("Run ")
                .fgRgb(r, g, b)
                .a(String.format("%03d", runId % 1000))
                .fgDefault()
                .a('$')
                .reset()
                .toString();
    }

    public String formatTag(String tag, int length) {
        return String.format("%-" + length + "s", tag);
    }

    public String formatStepNumber(int stepNumber) {
        return String.format("%02d", stepNumber);
    }

    public String formatCommitSha(String commitSha) {
        return commitSha.substring(0, COMMIT_SHA_LENGTH);
    }

    public String formatName(String name) {
        if (name.length() > MAX_NAME_LENGTH) {
            return name.substring(0, MAX_NAME_LENGTH - TEXT_OVERFLOW.length()) + TEXT_OVERFLOW;
        }
        return name;
    }

    public String prettyPrintOnSeparateLines(List<Event> events) {
        return String.join("\n",
                events.stream().map(Event::prettyPrint).toList()
        );
    }

    public String stripAnsi(String ansiString) {
        return ansiString.replaceAll(ANSI_REGEX, "");
    }

    public String getRepeatedString(String pattern, int length) {
        return new String(new char[length]).replace("\0", pattern);
    }

    public String getPaddingBeforeBranch(String ansiStringBefore, String branchName) {
        int lengthBefore = stripAnsi(ansiStringBefore).length();
        int budgetLength = LINE_LENGTH - COMMIT_SHA_LENGTH - Math.min(13, branchName.length()) - 1 - lengthBefore;
        return getRepeatedString(" ", Math.max(budgetLength, 1));
    }
}
