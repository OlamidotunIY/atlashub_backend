package com.atlashub.iam.application.commands.IssueApiKey;

import com.atlashub.iam.application.port.ApiSecretProtector;
import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.exception.LiveApiKeyUnavailableException;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.shared.application.port.ComplianceQueryPort;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class IssueApiKeyHandlerTest {
    @Test
    void issues_test_key_for_selected_organization_role_without_compliance_approval() {
        ApiKeyRepository keys = mock(ApiKeyRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        ComplianceQueryPort compliance = mock(ComplianceQueryPort.class);
        ApiSecretProtector protector = mock(ApiSecretProtector.class);
        when(keys.nextIdentity()).thenReturn(1L);
        when(protector.encrypt(anyString())).thenReturn("cipher");
        when(roles.findById(4L)).thenReturn(Optional.of(
                CustomRole.create(4L, 2L, "Cashier", "Cashier", Set.of(8L), false, 3L)));

        var result = new IssueApiKeyHandler(keys, roles, compliance, protector)
                .execute(new IssueApiKeyCommand(2L, "Default", "TEST", 3L, 4L));

        assertEquals("TEST", result.environment());
        verify(keys).save(any());
        verify(roles).findById(4L);
        verifyNoInteractions(compliance);
    }

    @Test
    void rejects_live_key_when_compliance_is_not_approved() {
        ApiKeyRepository keys = mock(ApiKeyRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        ComplianceQueryPort compliance = mock(ComplianceQueryPort.class);
        ApiSecretProtector protector = mock(ApiSecretProtector.class);
        when(compliance.isApproved(2L)).thenReturn(false);

        assertThrows(LiveApiKeyUnavailableException.class, () ->
                new IssueApiKeyHandler(keys, roles, compliance, protector)
                        .execute(new IssueApiKeyCommand(2L, "Production", "LIVE", 3L, 4L)));

        verifyNoInteractions(roles, keys, protector);
    }
}
