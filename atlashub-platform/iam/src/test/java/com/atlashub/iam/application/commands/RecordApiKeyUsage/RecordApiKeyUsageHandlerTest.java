package com.atlashub.iam.application.commands.RecordApiKeyUsage;

import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class RecordApiKeyUsageHandlerTest {
    @Test
    void records_usage_for_scoped_public_key() {
        ApiKeyRepository repository = mock(ApiKeyRepository.class);
        ZonedDateTime now = ZonedDateTime.now();
        ApiKey key = new ApiKey(1L, 2L, "public", "cipher", "Key", ApiEnvironment.TEST,
                null, false, null, null, null, now, now);
        when(repository.findByPublicKey("public")).thenReturn(Optional.of(key));

        new RecordApiKeyUsageHandler(repository).execute(new RecordApiKeyUsageCommand(2L, "public"));

        assertNotNull(key.getLastUsedAt());
        verify(repository).save(key);
    }
}
