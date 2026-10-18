package com.atlashub.pay.transfers.application.commands.InitiateBulkPayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitiateBulkPayoutHandlerTest {

    @Mock
    private PayoutRepository payoutRepository;

    private InitiateBulkPayoutHandler handler;

    @BeforeEach
    void setUp() {
        handler = new InitiateBulkPayoutHandler(payoutRepository);
    }

    @Test
    @DisplayName("Should create batch of payouts requiring approval")
    void shouldInitiateBulkPayout() {
        when(payoutRepository.nextIdentity()).thenReturn(101L, 102L);

        List<InitiateBulkPayoutCommand.BulkPayoutItem> items = List.of(
                new InitiateBulkPayoutCommand.BulkPayoutItem(
                        "044", "0123456789", "Alice", new BigDecimal("50000.00"), "NGN", "Salary", "PAYROLL-ITEM-1"
                ),
                new InitiateBulkPayoutCommand.BulkPayoutItem(
                        "058", "0987654321", "Bob", new BigDecimal("75000.00"), "NGN", "Salary", "PAYROLL-ITEM-2"
                )
        );

        InitiateBulkPayoutCommand command = new InitiateBulkPayoutCommand(
                10L, 100L, "BATCH-2026-05", SourceSystem.PAYROLL, "RUN-42", items
        );

        InitiateBulkPayoutResponse response = handler.execute(command);

        assertEquals("BATCH-2026-05", response.bulkReference());
        assertEquals(2, response.totalCount());
        assertEquals(List.of(101L, 102L), response.payoutIds());
        verify(payoutRepository, times(2)).save(any(Payout.class));
    }
}
