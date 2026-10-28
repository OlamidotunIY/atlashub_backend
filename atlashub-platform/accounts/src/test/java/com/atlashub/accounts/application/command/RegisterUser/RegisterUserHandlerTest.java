package com.atlashub.accounts.application.command.RegisterUser;

import com.atlashub.accounts.domain.entities.User;
import com.atlashub.accounts.domain.repositories.UserRepository;
import com.atlashub.shared.application.port.OneTimeSecretStore;
import com.atlashub.shared.domain.valueobject.Country;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterUserHandlerTest {
    @Test
    void registration_creates_only_a_user_and_a_one_time_credential_claim() {
        UserRepository users = mock(UserRepository.class);
        OneTimeSecretStore secrets = mock(OneTimeSecretStore.class);
        when(users.findByEmail("tolu@example.com")).thenReturn(Optional.empty());
        when(users.nextIdentity()).thenReturn(11L);
        when(secrets.store("Strong1!", Duration.ofHours(24))).thenReturn("credential-reference");

        Long id = new RegisterUserHandler(users, secrets).execute(
                new RegisterUserCommand("Tolu", "Ade", "tolu@example.com", "Strong1!", new Country("NG")));

        assertEquals(11L, id);
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertNull(saved.getValue().getActiveOrganizationId());
    }
}
