package com.atlashub.pay.splits.domain.entities;

import com.atlashub.pay.splits.domain.exceptions.InvalidSplitPercentagesException;
import com.atlashub.pay.splits.domain.exceptions.MissingSubaccountException;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleInactiveException;
import com.atlashub.pay.splits.domain.valueobject.RecipientType;
import com.atlashub.pay.splits.domain.valueobject.SplitType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SplitRuleTest {

    @Test
    void create_validPercentageRule_success() {
        List<SplitSubaccount> subaccounts = new ArrayList<>();
        subaccounts.add(new SplitSubaccount(1L, null, RecipientType.VENDOR, "vendor1", new BigDecimal("80"), "Vendor share"));
        subaccounts.add(new SplitSubaccount(2L, null, RecipientType.ORGANIZATION, "org1", new BigDecimal("10"), "Org share"));

        SplitRule rule = SplitRule.create(
                1L,
                100L,
                "Standard Split",
                SplitType.PERCENTAGE,
                new BigDecimal("10"),
                subaccounts
        );

        assertNotNull(rule);
        assertEquals(1L, rule.getId());
        assertTrue(rule.isActive());
        assertEquals("Standard Split", rule.getName());
        assertEquals(SplitType.PERCENTAGE, rule.getType());
        assertEquals(2, rule.getSubaccounts().size());
    }

    @Test
    void create_percentageRuleNotSummingTo100_throwsException() {
        List<SplitSubaccount> subaccounts = new ArrayList<>();
        subaccounts.add(new SplitSubaccount(1L, null, RecipientType.VENDOR, "vendor1", new BigDecimal("80"), "Vendor share"));

        assertThrows(InvalidSplitPercentagesException.class, () -> {
            SplitRule.create(
                    1L,
                    100L,
                    "Invalid Split",
                    SplitType.PERCENTAGE,
                    new BigDecimal("10"),
                    subaccounts
            );
        });
    }

    @Test
    void create_noSubaccounts_throwsException() {
        assertThrows(MissingSubaccountException.class, () -> {
            SplitRule.create(
                    1L,
                    100L,
                    "No Subaccounts",
                    SplitType.FLAT,
                    new BigDecimal("10"),
                    Collections.emptyList()
            );
        });
    }

    @Test
    void update_activeRule_success() {
        List<SplitSubaccount> subaccounts = new ArrayList<>();
        subaccounts.add(new SplitSubaccount(1L, null, RecipientType.VENDOR, "vendor1", new BigDecimal("90"), "Vendor share"));

        SplitRule rule = SplitRule.create(
                1L,
                100L,
                "Standard Split",
                SplitType.PERCENTAGE,
                new BigDecimal("10"),
                subaccounts
        );

        List<SplitSubaccount> newSubaccounts = new ArrayList<>();
        newSubaccounts.add(new SplitSubaccount(1L, null, RecipientType.VENDOR, "vendor1", new BigDecimal("80"), "Vendor share"));
        newSubaccounts.add(new SplitSubaccount(2L, null, RecipientType.ORGANIZATION, "org1", new BigDecimal("10"), "Org share"));

        rule.update("Updated Split", SplitType.PERCENTAGE, new BigDecimal("10"), newSubaccounts);

        assertEquals("Updated Split", rule.getName());
        assertEquals(2, rule.getSubaccounts().size());
    }

    @Test
    void update_inactiveRule_throwsException() {
        List<SplitSubaccount> subaccounts = new ArrayList<>();
        subaccounts.add(new SplitSubaccount(1L, null, RecipientType.VENDOR, "vendor1", new BigDecimal("90"), "Vendor share"));

        SplitRule rule = SplitRule.create(
                1L,
                100L,
                "Standard Split",
                SplitType.PERCENTAGE,
                new BigDecimal("10"),
                subaccounts
        );

        rule.deactivate();

        assertThrows(SplitRuleInactiveException.class, () -> {
            rule.update("Updated Split", SplitType.PERCENTAGE, new BigDecimal("10"), subaccounts);
        });
    }

    @Test
    void removeSubaccount_leavesAtLeastOne_success() {
        List<SplitSubaccount> subaccounts = new ArrayList<>();
        subaccounts.add(new SplitSubaccount(1L, null, RecipientType.VENDOR, "vendor1", new BigDecimal("80"), "Vendor share"));
        subaccounts.add(new SplitSubaccount(2L, null, RecipientType.ORGANIZATION, "org1", new BigDecimal("10"), "Org share"));

        SplitRule rule = SplitRule.create(
                1L,
                100L,
                "Standard Split",
                SplitType.FLAT,
                new BigDecimal("10"),
                subaccounts
        );

        rule.removeSubaccount(1L);
        assertEquals(1, rule.getSubaccounts().size());
        assertEquals(2L, rule.getSubaccounts().get(0).getId());
    }

    @Test
    void removeSubaccount_leavesZero_throwsException() {
        List<SplitSubaccount> subaccounts = new ArrayList<>();
        subaccounts.add(new SplitSubaccount(1L, null, RecipientType.VENDOR, "vendor1", new BigDecimal("90"), "Vendor share"));

        SplitRule rule = SplitRule.create(
                1L,
                100L,
                "Standard Split",
                SplitType.FLAT,
                new BigDecimal("10"),
                subaccounts
        );

        assertThrows(MissingSubaccountException.class, () -> {
            rule.removeSubaccount(1L);
        });
    }
}
