package com.atlashub.pay.transfers.application.queries.ListPayouts;

import com.atlashub.pay.transfers.application.queries.GetPayout.PayoutResult;
import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPayoutsHandlerTest {

    @Mock
    private PayoutRepository payoutRepository;

    private ListPayoutsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListPayoutsHandler(payoutRepository);
    }

    @Test
    @DisplayName("Should list payouts with status filter")
    void shouldListPayoutsWithStatusFilter() {
        Payout payout = Payout.create(
                1L, 10L, Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                "044", "0123456789", "John Doe", "Test", "REF-001",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-1", 100L, false
        );

        when(payoutRepository.findByOrganizationIdAndStatus(10L, PayoutStatus.APPROVED, 1, 10))
                .thenReturn(new PageResult<>(List.of(payout), 1, 10, 1L, 1));

        PageResult<PayoutResult> result = handler.execute(new ListPayoutsQuery(10L, PayoutStatus.APPROVED, 1, 10));

        assertEquals(1, result.content().size());
        assertEquals("REF-001", result.content().get(0).reference());
        assertEquals(1L, result.totalElements());
    }

    @Test
    @DisplayName("Should list payouts without status filter")
    void shouldListPayoutsWithoutStatusFilter() {
        Payout payout = Payout.create(
                2L, 10L, Money.of(new BigDecimal("75000.00"), CurrencyCode.NGN),
                "044", "0123456789", "John Doe", "Test", "REF-002",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-2", 100L, false
        );

        when(payoutRepository.findByOrganizationId(10L, 1, 10))
                .thenReturn(new PageResult<>(List.of(payout), 1, 10, 1L, 1));

        PageResult<PayoutResult> result = handler.execute(new ListPayoutsQuery(10L, null, 1, 10));

        assertEquals(1, result.content().size());
        assertEquals("REF-002", result.content().get(0).reference());
    }
}
