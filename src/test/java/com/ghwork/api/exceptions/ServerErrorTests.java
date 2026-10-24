package com.ghwork.api.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ServerErrorTests {

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503, 504})
    public void isRetriableTest(int code) {
        assertTrue(new ServerError(code).isRetriable());
    }

    @ParameterizedTest
    @ValueSource(ints = {501, 505, 511, 599})
    public void isNotRetriableTest(int code) {
        assertFalse(new ServerError(code).isRetriable());
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 499, 600})
    public void rejectsNonServerErrorCodesTest(int code) {
        assertThrows(IllegalArgumentException.class, () -> new ServerError(code));
    }
}
