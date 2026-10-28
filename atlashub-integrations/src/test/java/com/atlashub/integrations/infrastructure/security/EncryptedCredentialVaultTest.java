package com.atlashub.integrations.infrastructure.security;

import com.atlashub.integrations.infrastructure.persistence.entities.CredentialJpa;
import com.atlashub.integrations.infrastructure.persistence.repositories.SpringDataCredentialRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EncryptedCredentialVaultTest {
    @Test
    void ciphertext_and_reference_never_contain_the_secret_and_round_trip_is_authenticated() {
        SpringDataCredentialRepository rows = mock(SpringDataCredentialRepository.class);
        String encoded = Base64.getEncoder().encodeToString(new byte[32]);
        EncryptedCredentialVault vault = new EncryptedCredentialVault(rows, () -> encoded);
        String reference = vault.store("sk_test_sensitive");
        ArgumentCaptor<CredentialJpa> saved = ArgumentCaptor.forClass(CredentialJpa.class);
        verify(rows).save(saved.capture());
        assertFalse(reference.contains("sk_test_sensitive"));
        assertFalse(saved.getValue().ciphertext.contains("sk_test_sensitive"));
        when(rows.findById(reference)).thenReturn(Optional.of(saved.getValue()));
        assertEquals("sk_test_sensitive", vault.read(reference));

        saved.getValue().ciphertext = Base64.getEncoder().encodeToString(new byte[16]);
        assertThrows(IllegalStateException.class, () -> vault.read(reference));
    }
}
