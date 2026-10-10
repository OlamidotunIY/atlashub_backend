package com.atlashub.accounting.gl.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlValueObjectsTest {

    @Test
    @DisplayName("Should verify AccountType values")
    void shouldVerifyAccountTypeValues() {
        assertNotNull(AccountType.valueOf("ASSET"));
        assertNotNull(AccountType.valueOf("LIABILITY"));
        assertNotNull(AccountType.valueOf("EQUITY"));
        assertNotNull(AccountType.valueOf("REVENUE"));
        assertNotNull(AccountType.valueOf("EXPENSE"));
        assertTrue(AccountType.values().length >= 5);
    }

    @Test
    @DisplayName("Should verify JournalEntryStatus values")
    void shouldVerifyJournalEntryStatusValues() {
        assertNotNull(JournalEntryStatus.valueOf("DRAFT"));
        assertNotNull(JournalEntryStatus.valueOf("PENDING_APPROVAL"));
        assertNotNull(JournalEntryStatus.valueOf("POSTED"));
        assertNotNull(JournalEntryStatus.valueOf("VOIDED"));
    }

    @Test
    @DisplayName("Should verify EntrySource values")
    void shouldVerifyEntrySourceValues() {
        assertNotNull(EntrySource.valueOf("AUTOMATIC"));
        assertNotNull(EntrySource.valueOf("MANUAL"));
    }

    @Test
    @DisplayName("Should verify EntryType values")
    void shouldVerifyEntryTypeValues() {
        assertNotNull(EntryType.valueOf("DEBIT"));
        assertNotNull(EntryType.valueOf("CREDIT"));
    }

    @Test
    @DisplayName("Should verify SourceSystem values")
    void shouldVerifySourceSystemValues() {
        assertNotNull(SourceSystem.valueOf("COMMERCE_CHECKOUT"));
        assertNotNull(SourceSystem.valueOf("PAYROLL"));
        assertNotNull(SourceSystem.valueOf("CASH_BANKING"));
        assertNotNull(SourceSystem.valueOf("SETTLEMENT"));
    }
}
