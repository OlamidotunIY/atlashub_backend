package com.atlashub.iam.application.commands.IssueApiKey;

import com.atlashub.iam.application.port.ApiSecretProtector;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.shared.application.port.ComplianceQueryPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class IssueApiKeyHandlerTest {
    @Test
    void issues_unbound_test_key_without_compliance_or_role_lookup() {
        ApiKeyRepository keys = mock(ApiKeyRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        ComplianceQueryPort compliance = mock(ComplianceQueryPort.class);
        ApiSecretProtector protector = mock(ApiSecretProtector.class);
        when(keys.nextIdentity()).thenReturn(1L);
        when(protector.encrypt(anyString())).thenReturn("cipher");

        var result = new IssueApiKeyHandler(keys, roles, compliance, protector)
                .execute(new IssueApiKeyCommand(2L, "Default", "TEST", 3L, null));

        assertEquals("TEST", result.environment());
        verify(keys).save(any());
        verifyNoInteractions(roles, compliance);
    }
}
