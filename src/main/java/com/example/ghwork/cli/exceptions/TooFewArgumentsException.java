package com.example.ghwork.cli.exceptions;

/**
 * Thrown when the user enters less than two arguments in the command line.
 */
public class TooFewArgumentsException extends CLIException {

    public TooFewArgumentsException(int numberOfArguments) {
        super("Too few arguments provided to the command. Expected 2, got " + numberOfArguments + ".");
    }
}
