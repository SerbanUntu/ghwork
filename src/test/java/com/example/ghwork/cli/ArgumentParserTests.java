package com.example.ghwork.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.example.ghwork.cli.exceptions.TooFewArgumentsException;
import com.example.ghwork.cli.exceptions.TooManyArgumentsException;
import com.example.ghwork.domain.AccessToken;
import com.example.ghwork.domain.Repository;
import org.junit.jupiter.api.Test;
public class ArgumentParserTests {

    private final ArgumentParser sut = new ArgumentParser();

    @Test
    public void parseNullTest() {
        assertThrows(TooFewArgumentsException.class, () -> sut.parse(null));
    }

    @Test
    public void tooFewArgumentsTest() {
        String[] args = {"arg1"};
        assertThrows(TooFewArgumentsException.class, () -> sut.parse(args));
    }

    @Test
    public void tooManyArgumentsTest() {
        String[] args = {"arg1", "arg2", "arg3"};
        assertThrows(TooManyArgumentsException.class, () -> sut.parse(args));
    }

    @Test
    public void parseTwoArgumentsCorrectlyTest() {
        String[] args = {"owner/repo", "accessToken"};
        CLIArguments expected = new CLIArguments(
                new Repository("owner/repo"),
                new AccessToken("accessToken")
        );
        assertEquals(expected, sut.parse(args));
    }
}
