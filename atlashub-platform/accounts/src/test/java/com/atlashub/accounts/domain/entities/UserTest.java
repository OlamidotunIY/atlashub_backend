package com.atlashub.accounts.domain.entities;

import com.atlashub.accounts.domain.events.UserCreated;
import com.atlashub.accounts.domain.exceptions.InvalidUserException;
import com.atlashub.accounts.domain.exceptions.WeakPasswordException;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    void publishes_only_the_one_time_credential_reference() {
        User user = User.create(
                1L, "Tolu", "Ade", new EmailAddress("tolu@example.com"),
                new Country("NG"), false, "Strong1!", "credential-reference");

        UserCreated event = (UserCreated) user.pullDomainEvents().getFirst();
        assertEquals("credential-reference", event.payload().credentialReference());
        assertEquals("tolu@example.com", event.payload().email());
    }

    @Test
    void rejects_weak_passwords_with_a_module_exception() {
        assertThrows(WeakPasswordException.class, () -> User.create(
                1L, "Tolu", "Ade", new EmailAddress("tolu@example.com"),
                new Country("NG"), false, "password", "credential-reference"));
    }

    @Test
    void rejects_missing_identity_with_a_module_exception() {
        assertThrows(InvalidUserException.class, () -> User.create(
                1L, " ", "Ade", new EmailAddress("tolu@example.com"),
                new Country("NG"), false, "Strong1!", "credential-reference"));
    }
}
