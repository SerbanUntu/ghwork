package com.example.github_workflow_tool.cli;

import com.example.github_workflow_tool.domain.events.Event;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles utilities for string padding and formatting for the events' data
 */
public class CLIPrinter {

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd:HH:mm:ss.SSSX";
    private static final String ANSI_REGEX = "\\x1b\\[[0-9;]*[a-zA-Z]";
    private static final int COMMIT_SHA_LENGTH = 7;
    private static final int MAX_NAME_LENGTH = 13;
    private static final String TEXT_OVERFLOW = "...";
    private static final int LINE_LENGTH = 110;

    //map run to id and vice-versa

    public String formatTimestamp(Instant timestamp) {
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT)
                .withZone(ZoneId.systemDefault());
        return timestampFormatter.format(timestamp);
    }

    public String formatRunId(long runId) {
        return "Run " + String.format("%03d$", runId % 1000);
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
