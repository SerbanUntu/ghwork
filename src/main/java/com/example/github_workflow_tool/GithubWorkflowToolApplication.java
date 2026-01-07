package com.example.github_workflow_tool;

import com.example.github_workflow_tool.api.WorkflowService;
import com.example.github_workflow_tool.api.exceptions.APIException;
import com.example.github_workflow_tool.cli.ArgumentParser;
import com.example.github_workflow_tool.cli.CLIArguments;
import com.example.github_workflow_tool.cli.CLIPrinter;
import com.example.github_workflow_tool.cli.StorageService;
import com.example.github_workflow_tool.cli.exceptions.CLIException;
import com.example.github_workflow_tool.diffing.DiffingService;
import com.example.github_workflow_tool.domain.Repository;
import com.example.github_workflow_tool.domain.ToolState;
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

        try {
            CLIArguments parsedArgs = (new ArgumentParser()).parse(args);
            WorkflowService workflowService = new WorkflowService(
                    parsedArgs.repository(),
                    parsedArgs.accessToken()
            );
            DiffingService diffingService = new DiffingService();
            StorageService storageService = new StorageService();
            CLIPrinter cliPrinter = new CLIPrinter();

            Map<Repository, ToolState> toolStates = storageService.retrieve();
            Instant lastApiCallTimestamp = null;
            boolean toolRanBefore = true;
            if (!toolStates.containsKey(parsedArgs.repository())) {
                toolRanBefore = false;
                toolStates.put(parsedArgs.repository(), new ToolState(new HashMap<>(), new HashSet<>()));
            }

            while (!Thread.currentThread().isInterrupted()) {
                ToolState toolState = toolStates.get(parsedArgs.repository());
                long timeToWait = 0L;
                if (lastApiCallTimestamp != null) {
                    timeToWait = Math.max(0L, Instant
                            .now()
                            .until(lastApiCallTimestamp.plus(MINIMUM_REQUEST_INTERVAL), ChronoUnit.MILLIS));
                }
                try {
                    //noinspection BusyWait
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                lastApiCallTimestamp = Instant.now();

                Map<Long, WorkflowRunData> newState = workflowService.getCurrentRuns(toolState.ignoredRunIds());
                Set<Long> runIdsToIgnore = workflowService.getRunsIdsToIgnore(newState);

                if (!toolRanBefore) {
                    toolStates.put(parsedArgs.repository(), new ToolState(newState, runIdsToIgnore));
                    storageService.save(toolStates);
                    toolRanBefore = true;
                    continue;
                }
                List<WorkflowRun> runsNotInTheNewState = workflowService.getRunsSetDifference(
                        toolState.runs(), newState, toolState.ignoredRunIds()
                );

                // Ensures augmentedState is a superset of currentState
                Map<Long, WorkflowRunData> augmentedNewState = workflowService.askForAdditionalRunData(
                        newState, runsNotInTheNewState
                );
                List<Event> workflowEvents = diffingService.computeDiff(toolState.runs(), augmentedNewState);
                if (!workflowEvents.isEmpty()) {
                    System.out.println(cliPrinter.prettyPrintOnSeparateLines(workflowEvents));
                }

                toolState.ignoredRunIds().addAll(runIdsToIgnore);
                // We don't need to keep tracking the runs that are not in newState, since they must have completed
                toolStates.put(parsedArgs.repository(), new ToolState(newState, toolState.ignoredRunIds()));
                storageService.save(toolStates);
            }
        } catch (APIException | CLIException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}