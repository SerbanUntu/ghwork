package com.ghwork.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.ghwork.cli.exceptions.TooFewArgumentsException;
import com.ghwork.cli.exceptions.TooManyArgumentsException;
import com.ghwork.domain.AccessToken;
import com.ghwork.domain.Repository;
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
        CliArguments expected = new CliArguments(
                new Repository("owner/repo"),
                new AccessToken("accessToken")
        );
        assertEquals(expected, sut.parse(args));
    }
}
