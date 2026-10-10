package com.atlashub.pay.settlement.application.commands;

import com.atlashub.pay.settlement.application.commands.DisputeSettlement.DisputeSettlementCommand;
import com.atlashub.pay.settlement.application.commands.DisputeSettlement.DisputeSettlementHandler;
import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.exceptions.SettlementNotFoundException;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DisputeSettlementHandlerTest {

    private SettlementRepository repository;
    private DisputeSettlementHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(SettlementRepository.class);
        handler = new DisputeSettlementHandler(repository);
    }

    private Settlement createAwaitingSettlement() {
        return Settlement.create(
                100L,
                10L,
                PaymentProvider.PAYSTACK,
                "BATCH_001",
                new Money(new BigDecimal("10000.00"), CurrencyCode.NGN),
                new Money(new BigDecimal("9850.00"), CurrencyCode.NGN),
                new Money(new BigDecimal("150.00"), CurrencyCode.NGN),
                20L,
                ZonedDateTime.now(),
                "test"
        );
    }

    @Test
    void execute_validCommand_disputesAndSavesSettlement() {
        Settlement settlement = createAwaitingSettlement();
        when(repository.findById(100L)).thenReturn(Optional.of(settlement));

        DisputeSettlementCommand command = new DisputeSettlementCommand(100L, 10L, ApiEnvironment.LIVE, "Excessive fee deduction");

        Void result = handler.execute(command);

        assertNull(result);
        assertTrue(settlement.isDisputed());
        verify(repository).save(settlement);
    }

    @Test
    void execute_settlementNotFound_throwsSettlementNotFoundException() {
        when(repository.findById(100L)).thenReturn(Optional.empty());

        DisputeSettlementCommand command = new DisputeSettlementCommand(100L, 10L, ApiEnvironment.LIVE, "Excessive fee deduction");

        assertThrows(SettlementNotFoundException.class, () -> handler.execute(command));
    }
}
