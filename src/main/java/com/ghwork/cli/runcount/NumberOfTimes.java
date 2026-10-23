package com.ghwork.cli.runcount;

/**
 * Indicates that the application polling loop should run a finite number of times.
 *
 * @param n The number of times the application polling loop should run before exiting with code 0.
 */
public record NumberOfTimes(int n) implements RunCount {
}
