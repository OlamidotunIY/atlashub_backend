package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.valueobject.IncentiveStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MerchantShippingIncentiveTest {

    @Test
    @DisplayName("Should compute 10% rebate amount from total net savings")
    void shouldComputeTenPercentRebate() {
        ZonedDateTime now = ZonedDateTime.now();
        Money totalBaseline = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        Money totalActual = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);

        MerchantShippingIncentive incentive = MerchantShippingIncentive.calculate(
                1L,
                100L,
                "2026-10",
                20,
                totalBaseline,
                totalActual,
                now
        );

        assertNotNull(incentive);
        assertEquals(1L, incentive.getId());
        assertEquals(100L, incentive.getOrganizationId());
        assertEquals("2026-10", incentive.getBillingYearMonth());
        assertEquals(20, incentive.getEligibleShipmentCount());
        assertEquals(new BigDecimal("0.10"), incentive.getIncentivePercentage());
        assertEquals(new BigDecimal("50000.0000"), incentive.getTotalNetSavings().amount());
        assertEquals(new BigDecimal("5000.0000"), incentive.getRebateAmount().amount());
        assertEquals(IncentiveStatus.CALCULATED, incentive.getStatus());
    }

    @Test
    @DisplayName("Should mark incentive as credited")
    void shouldMarkIncentiveCredited() {
        ZonedDateTime now = ZonedDateTime.now();
        Money totalBaseline = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        Money totalActual = Money.of(new BigDecimal("30000.00"), CurrencyCode.NGN);

        MerchantShippingIncentive incentive = MerchantShippingIncentive.calculate(
                2L,
                100L,
                "2026-09",
                10,
                totalBaseline,
                totalActual,
                now
        );

        incentive.markCredited(now.plusHours(1), "corr-cred-test");
        assertEquals(IncentiveStatus.CREDITED, incentive.getStatus());
        assertEquals(now.plusHours(1), incentive.getCreditedAt());
    }
}
