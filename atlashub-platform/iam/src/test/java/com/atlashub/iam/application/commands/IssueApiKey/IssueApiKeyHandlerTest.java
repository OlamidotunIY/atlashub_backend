package com.atlashub.iam.application.commands.IssueApiKey;

import com.atlashub.iam.application.port.ApiSecretProtector;
import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.exception.InvalidApiKeyException;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class IssueApiKeyHandlerTest {
    @Test
    void issues_test_key_for_selected_organization_role() {
        ApiKeyRepository keys = mock(ApiKeyRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        ApiSecretProtector protector = mock(ApiSecretProtector.class);
        when(keys.nextIdentity()).thenReturn(1L);
        when(protector.encrypt(anyString())).thenReturn("cipher");
        when(roles.findById(4L)).thenReturn(Optional.of(
                CustomRole.create(4L, 2L, "Cashier", "Cashier", Set.of(8L), false, 3L)));

        var result = new IssueApiKeyHandler(keys, roles, protector)
                .execute(new IssueApiKeyCommand(2L, "Default", ApiEnvironment.TEST, 3L, 4L));

        assertEquals("TEST", result.environment());
        verify(keys).save(any());
        verify(roles).findById(4L);
    }

    @Test
    void issues_live_key_for_selected_organization_role() {
        ApiKeyRepository keys = mock(ApiKeyRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        ApiSecretProtector protector = mock(ApiSecretProtector.class);
        when(keys.nextIdentity()).thenReturn(1L);
        when(protector.encrypt(anyString())).thenReturn("cipher");
        when(roles.findById(4L)).thenReturn(Optional.of(
                CustomRole.create(4L, 2L, "Cashier", "Cashier", Set.of(8L), false, 3L)));
        var result = new IssueApiKeyHandler(keys, roles, protector)
                .execute(new IssueApiKeyCommand(2L, "Production", ApiEnvironment.LIVE, 3L, 4L));
        assertEquals("LIVE", result.environment());
        verify(keys).save(any());
    }

    @Test
    void rejects_missing_role_outside_http_validation_boundary() {
        ApiKeyRepository keys = mock(ApiKeyRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        ApiSecretProtector protector = mock(ApiSecretProtector.class);

        assertThrows(InvalidApiKeyException.class, () ->
                new IssueApiKeyHandler(keys, roles, protector)
                        .execute(new IssueApiKeyCommand(2L, "Test", ApiEnvironment.TEST, 3L, null)));

        verifyNoInteractions(roles, keys, protector);
    }
}
