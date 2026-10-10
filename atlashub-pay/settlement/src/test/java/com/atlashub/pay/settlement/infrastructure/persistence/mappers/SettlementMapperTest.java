package com.atlashub.pay.settlement.infrastructure.persistence.mappers;

import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SettlementMapperTest {

    private SettlementMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new SettlementMapperImpl();
    }

    @Test
    @DisplayName("Should map domain Settlement to SettlementJpa correctly")
    void shouldMapDomainToPersistence() {
        ZonedDateTime now = ZonedDateTime.now();
        Settlement settlement = new Settlement(
                100L,
                200L,
                PaymentProvider.PAYSTACK,
                "SETTLE-12345",
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("9850.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("150.00"), CurrencyCode.NGN),
                300L,
                "TRF-REF-999",
                now.minusDays(1),
                SettlementStatus.CONFIRMED,
                "Batch payout",
                now.minusDays(2),
                now
        );

        SettlementJpa jpa = mapper.toPersistence(settlement);

        assertNotNull(jpa);
        assertEquals(100L, jpa.getId());
        assertEquals(200L, jpa.getOrganizationId());
        assertEquals(PaymentProvider.PAYSTACK, jpa.getProvider());
        assertEquals("SETTLE-12345", jpa.getProviderSettlementId());
        assertEquals(new BigDecimal("10000.0000"), jpa.getGrossAmount());
        assertEquals(new BigDecimal("9850.0000"), jpa.getNetAmount());
        assertEquals(new BigDecimal("150.0000"), jpa.getProviderFeeAmount());
        assertEquals(300L, jpa.getAnchorDepositAccountId());
        assertEquals("TRF-REF-999", jpa.getAnchorTransferReference());
        assertEquals(now.minusDays(1), jpa.getSettledAt());
        assertEquals(SettlementStatus.CONFIRMED, jpa.getStatus());
        assertEquals("Batch payout", jpa.getDescription());
        assertEquals(now.minusDays(2), jpa.getCreatedAt());
        assertEquals(now, jpa.getUpdatedAt());
        assertNull(jpa.getVersion());
    }

    @Test
    @DisplayName("Should map SettlementJpa to domain Settlement correctly")
    void shouldMapPersistenceToDomain() {
        ZonedDateTime now = ZonedDateTime.now();
        SettlementJpa jpa = new SettlementJpa(
                101L,
                201L,
                ApiEnvironment.LIVE,
                PaymentProvider.PAYSTACK,
                "MP-SETTLE-888",
                "SUB_888",
                new BigDecimal("50000.00"),
                new BigDecimal("49500.00"),
                new BigDecimal("500.00"),
                CurrencyCode.NGN,
                301L,
                List.of("TX_1"),
                "ANCHOR-TRF-001",
                now.minusHours(5),
                SettlementStatus.AWAITING_ANCHOR_CREDIT,
                "Moniepoint settlement",
                now.minusHours(6),
                now,
                1L
        );

        Settlement settlement = mapper.toDomain(jpa);

        assertNotNull(settlement);
        assertEquals(101L, settlement.getId());
        assertEquals(201L, settlement.getOrganizationId());
        assertEquals(PaymentProvider.PAYSTACK, settlement.getProvider());
        assertEquals("MP-SETTLE-888", settlement.getProviderSettlementId());
        assertEquals(new BigDecimal("50000.0000"), settlement.getGrossAmount().amount());
        assertEquals(CurrencyCode.NGN, settlement.getGrossAmount().currency());
        assertEquals(new BigDecimal("49500.0000"), settlement.getNetAmount().amount());
        assertEquals(CurrencyCode.NGN, settlement.getNetAmount().currency());
        assertEquals(new BigDecimal("500.0000"), settlement.getProviderFeeAmount().amount());
        assertEquals(CurrencyCode.NGN, settlement.getProviderFeeAmount().currency());
        assertEquals(301L, settlement.getAnchorDepositAccountId());
        assertEquals("ANCHOR-TRF-001", settlement.getAnchorTransferReference());
        assertEquals(now.minusHours(5), settlement.getSettledAt());
        assertEquals(SettlementStatus.AWAITING_ANCHOR_CREDIT, settlement.getStatus());
        assertEquals("Moniepoint settlement", settlement.getDescription());
        assertEquals(now.minusHours(6), settlement.getCreatedAt());
        assertEquals(now, settlement.getUpdatedAt());
    }

    @Test
    @DisplayName("Should perform full round trip mapping correctly")
    void shouldPerformRoundTripMapping() {
        ZonedDateTime now = ZonedDateTime.now();
        Settlement original = new Settlement(
                102L,
                202L,
                PaymentProvider.PAYSTACK,
                "SETTLE-ROUNDTRIP",
                Money.of(new BigDecimal("2000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1900.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("100.00"), CurrencyCode.NGN),
                302L,
                null,
                now.minusDays(1),
                SettlementStatus.AWAITING_ANCHOR_CREDIT,
                "Round trip test",
                now.minusDays(1),
                now
        );

        SettlementJpa jpa = mapper.toPersistence(original);
        Settlement mapped = mapper.toDomain(jpa);

        assertNotNull(mapped);
        assertEquals(original.getId(), mapped.getId());
        assertEquals(original.getOrganizationId(), mapped.getOrganizationId());
        assertEquals(original.getProvider(), mapped.getProvider());
        assertEquals(original.getProviderSettlementId(), mapped.getProviderSettlementId());
        assertEquals(original.getGrossAmount().amount(), mapped.getGrossAmount().amount());
        assertEquals(original.getNetAmount().amount(), mapped.getNetAmount().amount());
        assertEquals(original.getProviderFeeAmount().amount(), mapped.getProviderFeeAmount().amount());
        assertEquals(original.getAnchorDepositAccountId(), mapped.getAnchorDepositAccountId());
        assertNull(mapped.getAnchorTransferReference());
        assertEquals(original.getStatus(), mapped.getStatus());
        assertEquals(original.getDescription(), mapped.getDescription());
    }

    @Test
    @DisplayName("Should return null when source object is null")
    void shouldReturnNullWhenSourceIsNull() {
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toPersistence(null));
    }
}
