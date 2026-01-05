package com.example.github_workflow_tool;

import com.example.github_workflow_tool.api.GithubClient;
import com.example.github_workflow_tool.cli.ArgumentParser;
import com.example.github_workflow_tool.cli.CLIArguments;

public class GithubWorkflowToolApplication {

    public static void main(String[] args) {
        try {
            CLIArguments parsedArgs = (new ArgumentParser()).parse(args);
            GithubClient ghClient = new GithubClient(parsedArgs.repository(), parsedArgs.accessToken());
            System.out.println(ghClient.fetchData());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}