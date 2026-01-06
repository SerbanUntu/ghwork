package com.example.github_workflow_tool;

import com.example.github_workflow_tool.api.WorkflowService;
import com.example.github_workflow_tool.cli.ArgumentParser;
import com.example.github_workflow_tool.cli.CLIArguments;
import com.example.github_workflow_tool.cli.CLIPrinter;
import com.example.github_workflow_tool.diffing.DiffingService;
import com.example.github_workflow_tool.domain.WorkflowRunData;
import com.example.github_workflow_tool.domain.events.Event;

import java.util.*;

/**
 * The CLI application class. Contains the main() method which starts the application.
 */
public class GithubWorkflowToolApplication {

    /**
     * The starting point of the application.
     *
     * @param args The arguments passed to the CLI. Should be owner/repo and accessToken.
     */
    public static void main(String[] args) {
        try {
            Map<Long, WorkflowRunData> before = new HashMap<>();
            CLIArguments parsedArgs = (new ArgumentParser()).parse(args);
            WorkflowService workflowService = new WorkflowService(
                    parsedArgs.repository(),
                    parsedArgs.accessToken()
            );
            DiffingService diffingService = new DiffingService();
            List<Event> workflowEvents = diffingService.computeDiff(before, workflowService.queryApi());
            System.out.println((new CLIPrinter()).prettyPrintOnSeparateLines(workflowEvents));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}