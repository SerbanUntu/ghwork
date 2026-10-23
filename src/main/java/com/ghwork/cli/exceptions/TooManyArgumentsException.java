package com.ghwork.cli.exceptions;

/**
 * Thrown when the user enters more than two arguments in the command line.
 */
public class TooManyArgumentsException extends CliException {

    /**
     * Constructor for an exception thrown when the user enters more than two arguments
     * in the command line.
     */
    public TooManyArgumentsException(int numberOfArguments) {
        super("Too many arguments provided to the command. Expected 2, got "
            + numberOfArguments
            + ".");
    }
}
