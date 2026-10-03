package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

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
public class BankAccountNumberEncryptionConverter implements AttributeConverter<String, String> {
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private final byte[] masterKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public BankAccountNumberEncryptionConverter(
            @Value("${atlashub.pay.accounts.encryption-key:${ATLASHUB_PAY_ACCOUNTS_ENCRYPTION_KEY:}}")
            String encodedMasterKey
    ) {
        if (encodedMasterKey == null || encodedMasterKey.isBlank()) {
            this.masterKey = null;
            return;
        }
        try {
            this.masterKey = Base64.getDecoder().decode(encodedMasterKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Pay accounts encryption key must be valid Base64", exception);
        }
        if (masterKey.length != 32) {
            throw new IllegalStateException("Pay accounts encryption key must contain exactly 32 bytes");
        }
    }

    @Override
    public String convertToDatabaseColumn(String accountNumber) {
        if (accountNumber == null) return null;
        requireKey();
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(accountNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(ByteBuffer.allocate(iv.length + encrypted.length)
                            .put(iv).put(encrypted).array());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to encrypt bank account number", exception);
        }
    }

    @Override
    public String convertToEntityAttribute(String ciphertext) {
        if (ciphertext == null) return null;
        requireKey();
        try {
            ByteBuffer data = ByteBuffer.wrap(Base64.getUrlDecoder().decode(ciphertext));
            if (data.remaining() <= IV_LENGTH) {
                throw new IllegalStateException("Encrypted bank account number is malformed");
            }
            byte[] iv = new byte[IV_LENGTH];
            data.get(iv);
            byte[] encrypted = new byte[data.remaining()];
            data.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to decrypt bank account number", exception);
        }
    }

    private SecretKeySpec key() {
        return new SecretKeySpec(masterKey, "AES");
    }

    private void requireKey() {
        if (masterKey == null) {
            throw new IllegalStateException("ATLASHUB_PAY_ACCOUNTS_ENCRYPTION_KEY must be configured");
        }
    }
}
