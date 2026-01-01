package com.example.github_workflow_tool;

import com.example.github_workflow_tool.commands.HelloCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.EnableCommand;

@SpringBootApplication
@EnableCommand({ HelloCommand.class })
public class GithubWorkflowToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(GithubWorkflowToolApplication.class, args);
    }
}