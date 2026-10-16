package com.atlashub.accounting.assets.domain.entities;

import com.atlashub.accounting.assets.domain.events.AssetDepreciatedEvent;
import com.atlashub.accounting.assets.domain.events.AssetDisposedEvent;
import com.atlashub.accounting.assets.domain.exceptions.AssetAlreadyDisposedException;
import com.atlashub.accounting.assets.domain.exceptions.AssetAlreadyFullyDepreciatedException;
import com.atlashub.accounting.assets.domain.exceptions.InvalidAssetOperationException;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetTest {

    @Test
    @DisplayName("Should create Asset with valid attributes and initial state")
    void shouldCreateAssetSuccessfully() {
        Asset asset = Asset.create(
                1L,
                10L,
                "MacBook Pro 16",
                "AST-001",
                "Computer Equipment",
                LocalDate.now(),
                Money.of(new BigDecimal("1200000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("200000.00"), CurrencyCode.NGN),
                36,
                DepreciationMethod.STRAIGHT_LINE
        );

        assertNotNull(asset);
        assertEquals(1L, asset.getId());
        assertEquals(10L, asset.getOrganizationId());
        assertEquals("MacBook Pro 16", asset.getName());
        assertEquals("AST-001", asset.getTagNumber());
        assertEquals(AssetStatus.ACTIVE, asset.getStatus());
        assertThat(asset.getCurrentBookValue().amount()).isEqualByComparingTo(new BigDecimal("1200000.00"));
        assertThat(asset.getAccumulatedDepreciation().amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should fail creation when invariants are violated")
    void shouldFailCreationOnInvalidInvariants() {
        assertThrows(InvalidAssetOperationException.class, () -> Asset.create(
                1L,
                null,
                "Laptop",
                "AST-001",
                "IT",
                LocalDate.now(),
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                12,
                DepreciationMethod.STRAIGHT_LINE
        ));

        assertThrows(InvalidAssetOperationException.class, () -> Asset.create(
                1L,
                10L,
                "",
                "AST-001",
                "IT",
                LocalDate.now(),
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                12,
                DepreciationMethod.STRAIGHT_LINE
        ));

        assertThrows(InvalidAssetOperationException.class, () -> Asset.create(
                1L,
                10L,
                "Laptop",
                "AST-001",
                "IT",
                LocalDate.now(),
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000.00"), CurrencyCode.NGN), // salvage > purchase
                12,
                DepreciationMethod.STRAIGHT_LINE
        ));

        assertThrows(InvalidAssetOperationException.class, () -> Asset.create(
                1L,
                10L,
                "Laptop",
                "AST-001",
                "IT",
                LocalDate.now(),
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                0, // useful life <= 0
                DepreciationMethod.STRAIGHT_LINE
        ));
    }

    @Test
    @DisplayName("Should depreciate asset and emit AssetDepreciatedEvent")
    void shouldDepreciateAsset() {
        Asset asset = Asset.create(
                1L,
                10L,
                "MacBook Pro 16",
                "AST-001",
                "Computer Equipment",
                LocalDate.now(),
                Money.of(new BigDecimal("1200000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("200000.00"), CurrencyCode.NGN),
                10,
                DepreciationMethod.STRAIGHT_LINE
        );

        Money monthly = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        asset.depreciate(monthly);

        assertThat(asset.getAccumulatedDepreciation().amount()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(asset.getCurrentBookValue().amount()).isEqualByComparingTo(new BigDecimal("1100000.00"));
        assertEquals(AssetStatus.ACTIVE, asset.getStatus());
        assertEquals(1, asset.peekDomainEvents().size());
        assertThat(asset.peekDomainEvents().getFirst()).isInstanceOf(AssetDepreciatedEvent.class);
    }

    @Test
    @DisplayName("Should transition to FULLY_DEPRECIATED when accumulated reaches max depreciable")
    void shouldTransitionToFullyDepreciated() {
        Asset asset = Asset.create(
                1L,
                10L,
                "MacBook Pro 16",
                "AST-001",
                "Computer Equipment",
                LocalDate.now(),
                Money.of(new BigDecimal("1200000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("200000.00"), CurrencyCode.NGN),
                10,
                DepreciationMethod.STRAIGHT_LINE
        );

        Money largeAmount = Money.of(new BigDecimal("1500000.00"), CurrencyCode.NGN);
        asset.depreciate(largeAmount);

        assertEquals(AssetStatus.FULLY_DEPRECIATED, asset.getStatus());
        assertThat(asset.getAccumulatedDepreciation().amount()).isEqualByComparingTo(new BigDecimal("1000000.00"));
        assertThat(asset.getCurrentBookValue().amount()).isEqualByComparingTo(new BigDecimal("200000.00"));

        assertThrows(AssetAlreadyFullyDepreciatedException.class, () -> asset.depreciate(Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN)));
    }

    @Test
    @DisplayName("Should dispose asset and emit AssetDisposedEvent")
    void shouldDisposeAssetSuccessfully() {
        Asset asset = Asset.create(
                1L,
                10L,
                "Delivery Van",
                "AST-002",
                "Vehicles",
                LocalDate.now().minusYears(2),
                Money.of(new BigDecimal("5000000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN),
                60,
                DepreciationMethod.STRAIGHT_LINE
        );

        Money saleValue = Money.of(new BigDecimal("4500000.00"), CurrencyCode.NGN);
        asset.dispose(saleValue, LocalDate.now());

        assertEquals(AssetStatus.DISPOSED, asset.getStatus());
        assertEquals(LocalDate.now(), asset.getDisposalDate());
        assertEquals(saleValue, asset.getSaleValue());
        assertEquals(1, asset.peekDomainEvents().size());
        assertThat(asset.peekDomainEvents().getFirst()).isInstanceOf(AssetDisposedEvent.class);

        assertThrows(AssetAlreadyDisposedException.class, () -> asset.dispose(saleValue, LocalDate.now()));
        assertThrows(AssetAlreadyDisposedException.class, () -> asset.depreciate(Money.of(new BigDecimal("100.00"), CurrencyCode.NGN)));
    }
}
