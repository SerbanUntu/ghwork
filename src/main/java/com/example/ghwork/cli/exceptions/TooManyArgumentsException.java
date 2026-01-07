package com.example.ghwork.cli.exceptions;

/**
 * Thrown when the user enters more than two arguments in the command line.
 */
public class TooManyArgumentsException extends CLIException {

    public TooManyArgumentsException(int numberOfArguments) {
        super("Too many arguments provided to the command. Expected 2, got " + numberOfArguments + ".");
    }
}
