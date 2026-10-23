package com.ghwork.api.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ClientErrorTests {

    @ParameterizedTest
    @ValueSource(ints = {408, 429})
    public void isRetriableTest(int code) {
        assertTrue(new ClientError(code).isRetriable());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 410, 422, 499})
    public void isNotRetriableTest(int code) {
        assertFalse(new ClientError(code).isRetriable());
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 399, 500})
    public void rejectsNonClientErrorCodesTest(int code) {
        assertThrows(IllegalArgumentException.class, () -> new ClientError(code));
    }
}
