package com.atlashub.compliance.infrastructure.persistence.adapters;

import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class ComplianceSensitiveDataConverterTest {
    @Test
    void encrypts_with_random_nonce_and_round_trips() {
        String key = Base64.getEncoder().encodeToString(new byte[32]);
        ComplianceSensitiveDataConverter converter = new ComplianceSensitiveDataConverter(key);
        String first = converter.convertToDatabaseColumn("22222222226");
        String second = converter.convertToDatabaseColumn("22222222226");
        assertNotEquals(first, second);
        assertEquals("22222222226", converter.convertToEntityAttribute(first));
    }
}
