package com.atlashub.authentication.domain.services;

import com.atlashub.authentication.domain.exceptions.AuthenticationInvariantException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordPolicyTest {

    @Test
    void accepts_a_strong_password() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("Strong1!"));
    }

    @Test
    void rejects_a_password_without_all_required_character_groups() {
        assertThrows(AuthenticationInvariantException.class,
                () -> PasswordPolicy.validate("password1"));
    }
}
