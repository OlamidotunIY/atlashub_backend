package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankAccountNumberEncryptionConverterTest {

    @Test
    void encrypts_with_random_nonce_and_round_trips() {
        String key = Base64.getEncoder().encodeToString(new byte[32]);
        BankAccountNumberEncryptionConverter converter = new BankAccountNumberEncryptionConverter(key);

        String first = converter.convertToDatabaseColumn("5031000017");
        String second = converter.convertToDatabaseColumn("5031000017");

        assertNotEquals(first, second);
        assertEquals("5031000017", converter.convertToEntityAttribute(first));
    }

    @Test
    void refuses_sensitive_data_when_key_is_missing() {
        BankAccountNumberEncryptionConverter converter = new BankAccountNumberEncryptionConverter("");

        assertThrows(IllegalStateException.class,
                () -> converter.convertToDatabaseColumn("5031000017"));
    }
}
