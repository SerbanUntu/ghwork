package com.example.github_workflow_tool;

import com.example.github_workflow_tool.api.WorkflowService;
import com.example.github_workflow_tool.cli.ArgumentParser;
import com.example.github_workflow_tool.cli.CLIArguments;
import com.example.github_workflow_tool.cli.CLIPrinter;
import com.example.github_workflow_tool.diffing.DiffingService;
import com.example.github_workflow_tool.domain.WorkflowRun;
import com.example.github_workflow_tool.domain.WorkflowRunData;
import com.example.github_workflow_tool.domain.events.Event;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * The CLI application class. Contains the main() method which starts the application.
 */
public class GithubWorkflowToolApplication {

    private static final Duration MINIMUM_REQUEST_INTERVAL = Duration.of(5000, ChronoUnit.MILLIS);

    /**
     * The starting point of the application.
     *
     * @param args The arguments passed to the CLI. Should be owner/repo and accessToken.
     */
    public static void main(String[] args) {

        Map<Long, WorkflowRunData> currentState = new HashMap<>();
        boolean toolRanBefore = false;

        try {
            CLIArguments parsedArgs = (new ArgumentParser()).parse(args);
            WorkflowService workflowService = new WorkflowService(
                    parsedArgs.repository(),
                    parsedArgs.accessToken()
            );
            DiffingService diffingService = new DiffingService();
            Instant lastApiCallTimestamp = null;

            while (true) {
                long timeToWait = 0L;
                if (lastApiCallTimestamp != null) {
                    timeToWait = Math.max(0L, Instant
                            .now()
                            .until(lastApiCallTimestamp.plus(MINIMUM_REQUEST_INTERVAL), ChronoUnit.MILLIS));
                }
                Thread.sleep(timeToWait);
                lastApiCallTimestamp = Instant.now();

                Map<Long, WorkflowRunData> newState = workflowService.queryApi();
                List<WorkflowRun> runsNotInTheNewState = currentState
                        .values()
                        .stream()
                        .map(WorkflowRunData::run)
                        .filter(run -> !newState.containsKey(run.id()))
                        .toList();

                // Ensures augmentedState is a superset of currentState
                Map<Long, WorkflowRunData> augmentedState = workflowService.askForAdditionalRunData(
                        newState, runsNotInTheNewState
                );
                List<Event> workflowEvents = diffingService.computeDiff(currentState, augmentedState);
                if (toolRanBefore) {
                    System.out.println((new CLIPrinter()).prettyPrintOnSeparateLines(workflowEvents));
                }

                // We don't need to keep tracking the runs that are not in newState, since they must have completed
                currentState = newState;
                toolRanBefore = true;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}