package com.example.github_workflow_tool.commands;

import org.springframework.shell.command.annotation.Command;

@Command
public class HelloCommand {

    @Command
    public String hello() {
        return "Hello";
    }
}
