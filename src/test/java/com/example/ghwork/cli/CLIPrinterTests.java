package com.example.ghwork.cli;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

public class CLIPrinterTests {

    private final CLIPrinter sut = new CLIPrinter();

    @Test
    public void formatTagNonEmptyTest() {
        assertEquals("WARNING   ", sut.formatTag("WARNING", 10));
    }

    @Test
    public void formatTagNullTest() {
        assertEquals("   ", sut.formatTag(null, 3));
    }

    @ParameterizedTest()
    @CsvSource({"02,2", "32,32", "502,502"})
    public void formatStepNumberSingleDigitTest(String expected, int input) {
        assertEquals(expected, sut.formatStepNumber(input));
    }

    @Test
    public void stripAnsiTest() {
        String ansiString = ansi().fgBlue().a("T").fgRed().cursorLeft(1).a("est").reset().toString();
        assertEquals("Test", sut.stripAnsi(ansiString));
    }

    @Test
    public void stripAnsiLeavesNormalStringsIntactTest() {
        assertEquals("Test", sut.stripAnsi("Test"));
    }

    @Test
    public void repeatPatternTest() {
        assertEquals("hellohellohello", sut.getRepeatedString("hello", 3));
    }

    @Test
    public void repeatNullTest() {
        assertEquals("  ", sut.getRepeatedString(null, 2));
    }
}
