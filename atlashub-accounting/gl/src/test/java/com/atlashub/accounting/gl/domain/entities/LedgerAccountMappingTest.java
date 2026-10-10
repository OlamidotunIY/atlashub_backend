package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LedgerAccountMappingTest {

    @Test
    @DisplayName("Should create and update ledger account mapping")
    void shouldCreateAndUpdateMapping() {
        LedgerAccountMapping mapping = LedgerAccountMapping.create(
                1L,
                10L,
                SourceSystem.COMMERCE_CHECKOUT,
                101L,
                401L,
                "POS checkout bridge"
        );

        assertEquals(1L, mapping.getId());
        assertEquals(10L, mapping.getOrganizationId());
        assertEquals(SourceSystem.COMMERCE_CHECKOUT, mapping.getSourceSystem());
        assertEquals(101L, mapping.getDebitAccountId());
        assertEquals(401L, mapping.getCreditAccountId());
        assertEquals("POS checkout bridge", mapping.getDescription());

        mapping.updateMapping(102L, 402L, "Updated POS mapping");
        assertEquals(102L, mapping.getDebitAccountId());
        assertEquals(402L, mapping.getCreditAccountId());
        assertEquals("Updated POS mapping", mapping.getDescription());
    }
}
