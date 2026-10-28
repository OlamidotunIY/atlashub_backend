package com.atlashub.integrations.infrastructure.security;

import com.atlashub.integrations.application.port.CredentialVaultPort;
import com.atlashub.integrations.infrastructure.persistence.entities.CredentialJpa;
import com.atlashub.integrations.infrastructure.persistence.repositories.SpringDataCredentialRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class EncryptedCredentialVault implements CredentialVaultPort {
    private static final String KEY_VARIABLE = "ATLAS_INTEGRATIONS_KEY";
    private final SpringDataCredentialRepository repository;
    private final Supplier<String> keySource;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public EncryptedCredentialVault(SpringDataCredentialRepository repository) {
        this(repository, () -> System.getenv(KEY_VARIABLE));
    }

    EncryptedCredentialVault(SpringDataCredentialRepository repository, Supplier<String> keySource) {
        this.repository = repository;
        this.keySource = keySource;
    }

    @Override
    public String store(String credential) {
        if (credential == null || credential.isBlank()) throw new IllegalArgumentException("Credential is required");
        try {
            byte[] nonce = new byte[12];
            random.nextBytes(nonce);
            CredentialJpa row = new CredentialJpa();
            row.reference = UUID.randomUUID().toString();
            row.nonce = Base64.getEncoder().encodeToString(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(row.reference.getBytes(StandardCharsets.UTF_8));
            row.ciphertext = Base64.getEncoder().encodeToString(
                    cipher.doFinal(credential.getBytes(StandardCharsets.UTF_8)));
            repository.save(row);
            return row.reference;
        } catch (Exception error) { throw new IllegalStateException("Credential encryption failed", error); }
    }

    @Override
    public String read(String reference) {
        CredentialJpa row = repository.findById(reference).orElseThrow();
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128,
                    Base64.getDecoder().decode(row.nonce)));
            cipher.updateAAD(row.reference.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(Base64.getDecoder().decode(row.ciphertext)), StandardCharsets.UTF_8);
        } catch (Exception error) { throw new IllegalStateException("Credential decryption failed", error); }
    }

    @Override
    public void delete(String reference) {
        if (reference != null && !reference.isBlank()) repository.deleteById(reference);
    }

    private SecretKeySpec key() {
        String encoded = keySource.get();
        if (encoded == null || encoded.isBlank())
            throw new IllegalStateException(KEY_VARIABLE + " must be configured before connecting providers");
        byte[] bytes = Base64.getDecoder().decode(encoded);
        if (bytes.length != 32) throw new IllegalStateException(KEY_VARIABLE + " must contain a 256-bit key");
        return new SecretKeySpec(bytes, "AES");
    }
}
