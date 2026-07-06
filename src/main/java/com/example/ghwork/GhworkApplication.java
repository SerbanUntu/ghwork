package com.example.ghwork;

import com.example.ghwork.api.WorkflowService;
import com.example.ghwork.api.exceptions.ApiException;
import com.example.ghwork.cli.ArgumentParser;
import com.example.ghwork.cli.CliArguments;
import com.example.ghwork.cli.CliPrinter;
import com.example.ghwork.cli.StorageService;
import com.example.ghwork.cli.exceptions.CliException;
import com.example.ghwork.diffing.DiffingService;
import com.example.ghwork.domain.Repository;
import com.example.ghwork.domain.ToolState;
import com.example.ghwork.domain.WorkflowRun;
import com.example.ghwork.domain.WorkflowRunData;
import com.example.ghwork.domain.events.Event;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The CLI application class. Contains the main() method which starts the application.
 */
public class GhworkApplication {

    private static final Duration MINIMUM_REQUEST_INTERVAL = Duration.of(5000, ChronoUnit.MILLIS);

    /**
     * The starting point of the application.
     *
     * @param args The arguments passed to the CLI. Should be owner/repo and accessToken.
     */
    public static void main(String[] args) {

        try {
            CliArguments parsedArgs = (new ArgumentParser()).parse(args);
            WorkflowService workflowService = new WorkflowService(
                    parsedArgs.repository(),
                    parsedArgs.accessToken()
            );
            DiffingService diffingService = new DiffingService();
            StorageService storageService = new StorageService();
            CliPrinter cliPrinter = new CliPrinter();

            Map<Repository, ToolState> toolStates = storageService.retrieve();
            Instant lastApiCallTimestamp = null;
            boolean toolRanBefore = true;
            if (!toolStates.containsKey(parsedArgs.repository())) {
                toolRanBefore = false;
                toolStates.put(
                        parsedArgs.repository(),
                        new ToolState(new HashMap<>(), new HashSet<>())
                );
            }

            while (!Thread.currentThread().isInterrupted()) {
                final ToolState toolState = toolStates.get(parsedArgs.repository());
                long timeToWait = 0L;
                if (lastApiCallTimestamp != null) {
                    timeToWait = Math.max(0L, Instant.now().until(
                            lastApiCallTimestamp.plus(MINIMUM_REQUEST_INTERVAL),
                            ChronoUnit.MILLIS)
                    );
                }
                try {
                    // noinspection BusyWait
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                lastApiCallTimestamp = Instant.now();

                Map<Long, WorkflowRunData> newState =
                        workflowService.getCurrentRuns(toolState.ignoredRunIds());
                Set<Long> runIdsToIgnore = workflowService.getRunsIdsToIgnore(newState);

                if (!toolRanBefore) {
                    toolStates.put(
                            parsedArgs.repository(),
                            new ToolState(newState, runIdsToIgnore)
                    );
                    storageService.save(toolStates);
                    toolRanBefore = true;
                    continue;
                }
                List<WorkflowRun> runsNotInTheNewState = workflowService.getRunsSetDifference(
                        toolState.runs(),
                        newState,
                        toolState.ignoredRunIds()
                );

                // Ensures augmentedState is a superset of currentState
                Map<Long, WorkflowRunData> augmentedNewState = workflowService
                        .askForAdditionalRunData(newState, runsNotInTheNewState);
                List<Event> workflowEvents = diffingService.computeDiff(
                        toolState.runs(),
                        augmentedNewState
                );
                if (!workflowEvents.isEmpty()) {
                    System.out.println(cliPrinter.prettyPrintOnSeparateLines(workflowEvents));
                }

                toolState.ignoredRunIds().addAll(runIdsToIgnore);
                // We don't need to keep tracking the runs that are not in newState,
                // since they must have completed
                toolStates.put(
                        parsedArgs.repository(),
                        new ToolState(newState, toolState.ignoredRunIds())
                );
                storageService.save(toolStates);
            }
        } catch (ApiException | CliException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}