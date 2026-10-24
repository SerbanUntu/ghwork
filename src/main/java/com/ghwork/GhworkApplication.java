package com.ghwork;

import com.ghwork.api.WorkflowService;
import com.ghwork.api.WorkflowServiceFactory;
import com.ghwork.api.exceptions.ApiException;
import com.ghwork.cli.ArgumentParser;
import com.ghwork.cli.CliArguments;
import com.ghwork.cli.CliPrinter;
import com.ghwork.cli.EnvService;
import com.ghwork.cli.StorageService;
import com.ghwork.cli.exceptions.CliException;
import com.ghwork.cli.exceptions.StorageException;
import com.ghwork.cli.runcount.Infinite;
import com.ghwork.cli.runcount.NumberOfTimes;
import com.ghwork.cli.runcount.RunCount;
import com.ghwork.diffing.DiffingService;
import com.ghwork.domain.Repository;
import com.ghwork.domain.ToolState;
import com.ghwork.domain.WorkflowRun;
import com.ghwork.domain.WorkflowRunData;
import com.ghwork.domain.events.Event;
import com.ghwork.ioc.AppConfig;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * The CLI application class. Contains the main() method which starts the application.
 */
public class GhworkApplication {

    private static final Logger LOG = LogManager.getLogger(GhworkApplication.class);

    /**
     * The core application logic, with injected parameters for testability.
     *
     * @param args The arguments passed to the CLI. Should be owner/repo and accessToken.
     * @param out The stream where normal results of the CLI tool will be displayed.
     * @param apiBaseRoute The protocol and domain of the URL.
     * @param runCount How many times the application polling loop runs before the application
     *                 exits.
     * @param filePath The location of the file where the cached runs will be stored, or empty for
     *                 the default path.
     * @return The application exit code.
     */
    public static int runApplication(String[] args, PrintStream out, String apiBaseRoute,
        RunCount runCount, Optional<Path> filePath) {
        try (AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(AppConfig.class)) {
            CliArguments parsedArgs = context.getBean(ArgumentParser.class).parse(args);
            WorkflowServiceFactory workflowServiceFactory =
                context.getBean(WorkflowServiceFactory.class);
            WorkflowService workflowService = workflowServiceFactory.create(parsedArgs.repository(),
                parsedArgs.accessToken(), apiBaseRoute);
            DiffingService diffingService = context.getBean(DiffingService.class);
            EnvService envService = context.getBean(EnvService.class);
            StorageService storageService =
                filePath.map(path -> new StorageService(envService, path))
                    .orElseGet(() -> context.getBean(StorageService.class));
            CliPrinter cliPrinter = context.getBean(CliPrinter.class);

            Map<Repository, ToolState> toolStates = storageService.retrieve();
            Instant lastApiCallTimestamp = null;
            boolean toolRanBefore = true;
            if (!toolStates.containsKey(parsedArgs.repository())) {
                LOG.info("No data found for this repository, creating a new cache.");
                toolRanBefore = false;
                toolStates.put(parsedArgs.repository(),
                    new ToolState(new HashMap<>(), new HashSet<>()));
            }

            int successfulPolls = 0;
            int consecutiveFailedPolls = 0;
            boolean runsInfinitely = false;
            int maxPolls = 0;
            switch (runCount) {
                case Infinite ignored -> runsInfinitely = true;
                case NumberOfTimes(int n) -> maxPolls = n;
            }
            while (!Thread.currentThread().isInterrupted()
                && (runsInfinitely || successfulPolls < maxPolls)) {
                if (consecutiveFailedPolls > envService.getMaxRetryAttempts()) {
                    throw new ApiException("Failed to contact the GitHub API "
                        + consecutiveFailedPolls + " times in a row. Aborting.");
                }
                final ToolState toolState = toolStates.get(parsedArgs.repository());
                long timeToWait = 0L;
                if (lastApiCallTimestamp != null) {
                    timeToWait = Math.max(0L, Instant.now().until(
                        lastApiCallTimestamp
                            .plus(envService.getMinimumRequestInterval().multipliedBy((long) Math
                                .pow(envService.getBackoffGrowthFactor(), consecutiveFailedPolls))),
                        ChronoUnit.MILLIS));
                }
                try {
                    // noinspection BusyWait
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                lastApiCallTimestamp = Instant.now();

                try {
                    Map<Long, WorkflowRunData> newState =
                        workflowService.getCurrentRuns(toolState.ignoredRunIds());
                    Set<Long> runIdsToIgnore = workflowService.getRunsIdsToIgnore(newState);

                    if (!toolRanBefore) {
                        toolStates.put(parsedArgs.repository(),
                            new ToolState(newState, runIdsToIgnore));
                        storageService.save(toolStates);
                        toolRanBefore = true;
                        successfulPolls++;
                        consecutiveFailedPolls = 0;
                        continue;
                    }
                    List<WorkflowRun> runsNotInTheNewState = workflowService.getRunsSetDifference(
                        toolState.runs(), newState, toolState.ignoredRunIds());

                    // Ensures augmentedState is a superset of currentState
                    Map<Long, WorkflowRunData> augmentedNewState =
                        workflowService.askForAdditionalRunData(newState, runsNotInTheNewState);
                    List<Event> workflowEvents =
                        diffingService.computeDiff(toolState.runs(), augmentedNewState);
                    if (!workflowEvents.isEmpty()) {
                        out.println(cliPrinter.prettyPrintOnSeparateLines(workflowEvents));
                    }

                    Set<Long> ignoredRunIds = new HashSet<>(toolState.ignoredRunIds());
                    ignoredRunIds.addAll(runIdsToIgnore);
                    // We don't need to keep tracking the runs that are not in newState,
                    // since they must have completed
                    toolStates.put(parsedArgs.repository(), new ToolState(newState, ignoredRunIds));
                    storageService.save(toolStates);
                    successfulPolls++;
                    consecutiveFailedPolls = 0;
                } catch (ApiException e) {
                    if (!e.isRetriable()) {
                        throw e;
                    }
                    consecutiveFailedPolls++;
                    LOG.warn("Polling failure ({}/{}){}. Error: {}", consecutiveFailedPolls,
                        envService.getMaxRetryAttempts() + 1,
                        consecutiveFailedPolls <= envService.getMaxRetryAttempts()
                            ? ", retrying"
                            : "",
                        e.getMessage());
                }
            }
        } catch (ApiException | CliException | StorageException e) {
            LOG.error("The system encountered an error: {}", e.getMessage());
            return 1;
        }
        return 0;
    }

    /**
     * The starting point of the application.
     *
     * @param args The arguments passed to the CLI. Should be owner/repo and accessToken.
     */
    public static void main(String[] args) {
        int exitCode = runApplication(args, System.out, "https://api.github.com", new Infinite(),
            Optional.empty());
        System.exit(exitCode);
    }
}
