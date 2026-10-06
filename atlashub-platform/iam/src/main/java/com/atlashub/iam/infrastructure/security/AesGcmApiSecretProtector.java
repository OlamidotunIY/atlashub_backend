package com.atlashub.iam.infrastructure.security;

import com.atlashub.iam.application.port.ApiSecretProtector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesGcmApiSecretProtector implements ApiSecretProtector {
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final String encodedMasterKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmApiSecretProtector(
            @Value("${atlashub.api-keys.master-key:${ATLASHUB_API_KEY_MASTER_KEY:}}") String encodedMasterKey) {
        this.encodedMasterKey = encodedMasterKey;
    }

    @Override
    public String encrypt(String secret) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to protect API secret", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        try {
            ByteBuffer data = ByteBuffer.wrap(Base64.getUrlDecoder().decode(ciphertext));
            byte[] iv = new byte[IV_LENGTH];
            data.get(iv);
            byte[] encrypted = new byte[data.remaining()];
            data.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Unable to read API secret", e);
        }
    }

    private SecretKeySpec key() {
        if (encodedMasterKey == null || encodedMasterKey.isBlank()) {
            throw new IllegalStateException("ATLASHUB_API_KEY_MASTER_KEY must be configured");
        }
        byte[] keyBytes = Base64.getDecoder().decode(encodedMasterKey);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("API key master key must contain exactly 32 bytes");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
}
