package com.atlashub.iam.application.commands.RotateApiKey;

import com.atlashub.iam.application.port.ApiSecretProtector;
import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RotateApiKeyHandlerTest {
    @Test
    void revokes_locked_key_and_creates_replacement_with_same_scope() {
        ApiKeyRepository repository = mock(ApiKeyRepository.class);
        ApiSecretProtector protector = mock(ApiSecretProtector.class);
        ZonedDateTime now = ZonedDateTime.now();
        ApiKey current = new ApiKey(1L, 2L, "old", "cipher", "Old", ApiEnvironment.TEST,
                4L, false, null, null, null, now, now);
        when(repository.findByIdAndOrganizationIdForUpdate(1L, 2L)).thenReturn(Optional.of(current));
        when(repository.nextIdentity()).thenReturn(5L);
        when(protector.encrypt(anyString())).thenReturn("new-cipher");

        var result = new RotateApiKeyHandler(repository, protector)
                .execute(new RotateApiKeyCommand(1L, 2L, 3L, "Replacement"));

        assertTrue(current.getRevoked());
        assertEquals("TEST", result.environment());
        ArgumentCaptor<ApiKey> saved = ArgumentCaptor.forClass(ApiKey.class);
        verify(repository, times(2)).save(saved.capture());
        ApiKey replacement = saved.getAllValues().get(1);
        assertEquals(4L, replacement.getBoundRoleId());
        assertEquals("Replacement", replacement.getName());
    }
}
