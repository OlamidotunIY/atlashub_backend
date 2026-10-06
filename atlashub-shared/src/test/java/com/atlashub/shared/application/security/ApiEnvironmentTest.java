package com.atlashub.shared.application.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiEnvironmentTest {

    @Test
    void parses_supported_environments_case_insensitively() {
        assertEquals(ApiEnvironment.TEST, ApiEnvironment.parse(" test "));
        assertEquals(ApiEnvironment.LIVE, ApiEnvironment.parse("LIVE"));
    }

    @Test
    void rejects_missing_or_unknown_environment() {
        assertThrows(IllegalArgumentException.class, () -> ApiEnvironment.parse(null));
        assertThrows(IllegalArgumentException.class, () -> ApiEnvironment.parse("sandbox"));
    }
}
