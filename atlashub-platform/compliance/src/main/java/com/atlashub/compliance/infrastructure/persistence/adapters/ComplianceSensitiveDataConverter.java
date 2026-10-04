package com.atlashub.compliance.infrastructure.persistence.adapters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class ComplianceSensitiveDataConverter implements AttributeConverter<String, String> {
    private static final int IV_LENGTH = 12;
    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public ComplianceSensitiveDataConverter(
            @Value("${atlashub.compliance.encryption-key:${ATLASHUB_COMPLIANCE_ENCRYPTION_KEY:}}") String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) { key = null; return; }
        try { key = Base64.getDecoder().decode(encodedKey); }
        catch (IllegalArgumentException error) { throw new IllegalStateException("Compliance encryption key must be valid Base64", error); }
        if (key.length != 32) throw new IllegalStateException("Compliance encryption key must contain exactly 32 bytes");
    }

    @Override public String convertToDatabaseColumn(String value) { return value == null ? null : encrypt(value); }
    @Override public String convertToEntityAttribute(String value) { return value == null ? null : decrypt(value); }

    private String encrypt(String value) {
        requireKey();
        try {
            byte[] iv = new byte[IV_LENGTH]; random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array());
        } catch (GeneralSecurityException error) { throw new IllegalStateException("Unable to encrypt compliance data", error); }
    }

    private String decrypt(String value) {
        requireKey();
        try {
            ByteBuffer data = ByteBuffer.wrap(Base64.getUrlDecoder().decode(value));
            if (data.remaining() <= IV_LENGTH) throw new IllegalStateException("Encrypted compliance data is malformed");
            byte[] iv = new byte[IV_LENGTH]; data.get(iv); byte[] encrypted = new byte[data.remaining()]; data.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException error) {
            throw new IllegalStateException("Unable to decrypt compliance data", error);
        }
    }

    private SecretKeySpec secretKey() { return new SecretKeySpec(key, "AES"); }
    private void requireKey() { if (key == null) throw new IllegalStateException("ATLASHUB_COMPLIANCE_ENCRYPTION_KEY must be configured"); }
}
