package com.atlashub.pay.transfers.application.queries.GetPayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.exceptions.PayoutNotFoundException;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPayoutHandlerTest {

    @Mock
    private PayoutRepository payoutRepository;

    private GetPayoutHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetPayoutHandler(payoutRepository);
    }

    @Test
    @DisplayName("Should retrieve payout result when orgId matches")
    void shouldGetPayoutSuccessfully() {
        Payout payout = Payout.create(
                1L, 10L, Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                "044", "0123456789", "John Doe", "Test", "REF-001",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-1", 100L, false
        );
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));

        PayoutResult result = handler.execute(new GetPayoutQuery(10L, 1L));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(10L, result.organizationId());
        assertEquals("REF-001", result.reference());
        assertEquals(0, new BigDecimal("50000.00").compareTo(result.amount()));
    }

    @Test
    @DisplayName("Should throw PayoutNotFoundException when orgId mismatches")
    void shouldThrowWhenOrgMismatch() {
        Payout payout = Payout.create(
                1L, 10L, Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                "044", "0123456789", "John Doe", "Test", "REF-001",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-1", 100L, false
        );
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));

        assertThrows(PayoutNotFoundException.class, () -> handler.execute(new GetPayoutQuery(999L, 1L)));
    }
}
