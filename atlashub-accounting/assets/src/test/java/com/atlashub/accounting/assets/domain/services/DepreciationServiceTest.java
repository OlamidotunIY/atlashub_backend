package com.atlashub.accounting.assets.domain.services;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepreciationServiceTest {

    private DepreciationService service;

    @BeforeEach
    void setUp() {
        service = new DepreciationService();
    }

    @Test
    @DisplayName("Should calculate monthly straight line depreciation accurately")
    void shouldCalculateMonthlyStraightLineDepreciation() {
        // Purchase: 1,200,000, Salvage: 200,000, Useful life: 10 months -> Depreciable = 1,000,000 / 10 = 100,000
        Asset asset = Asset.create(
                1L,
                10L,
                "Office Generator",
                "AST-GEN-01",
                "Machinery",
                LocalDate.now(),
                Money.of(new BigDecimal("1200000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("200000.00"), CurrencyCode.NGN),
                10,
                DepreciationMethod.STRAIGHT_LINE
        );

        Money monthly = service.calculateMonthlyDepreciation(asset);
        assertThat(monthly.amount()).isEqualByComparingTo(new BigDecimal("100000.0000"));
        assertFalse(service.isFullyDepreciated(asset));
    }

    @Test
    @DisplayName("Should cap depreciation when remaining amount is less than monthly rate")
    void shouldCapDepreciationAtRemainingAmount() {
        Asset asset = Asset.create(
                1L,
                10L,
                "Office Generator",
                "AST-GEN-01",
                "Machinery",
                LocalDate.now(),
                Money.of(new BigDecimal("1050000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN), // depreciable: 50,000
                1,
                DepreciationMethod.STRAIGHT_LINE
        );

        Money monthly = service.calculateMonthlyDepreciation(asset);
        assertThat(monthly.amount()).isEqualByComparingTo(new BigDecimal("50000.0000"));

        asset.depreciate(monthly);
        assertTrue(service.isFullyDepreciated(asset));
    }
}
