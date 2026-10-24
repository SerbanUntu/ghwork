package com.ghwork.cli.runcount;

/**
 * How many times the application polling loop runs before the application exits.
 */
public sealed interface RunCount permits Infinite, NumberOfTimes {
}
