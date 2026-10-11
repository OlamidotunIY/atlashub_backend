package com.atlashub.accounting.cash.application.command;

import com.atlashub.accounting.cash.application.command.ConfirmCashEvacuation.ConfirmCashEvacuationCommand;
import com.atlashub.accounting.cash.application.command.ConfirmCashEvacuation.ConfirmCashEvacuationHandler;
import com.atlashub.accounting.cash.domain.entities.CashEvacuation;
import com.atlashub.accounting.cash.domain.exceptions.CashEvacuationNotFoundException;
import com.atlashub.accounting.cash.domain.repositories.CashEvacuationRepository;
import com.atlashub.accounting.cash.domain.valueobject.EvacuationStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfirmCashEvacuationHandlerTest {

    private CashEvacuationRepository repository;
    private ConfirmCashEvacuationHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(CashEvacuationRepository.class);
        handler = new ConfirmCashEvacuationHandler(repository);
    }

    @Test
    void execute_confirmsPendingEvacuationSuccessfully() {
        CashEvacuation evacuation = CashEvacuation.create(
                1L, 10L, 20L, 30L, Money.of("10000.00", CurrencyCode.NGN), 40L
        );

        when(repository.findById(1L)).thenReturn(Optional.of(evacuation));

        ConfirmCashEvacuationCommand command = new ConfirmCashEvacuationCommand(10L, 1L);
        handler.execute(command);

        assertEquals(EvacuationStatus.CONFIRMED, evacuation.getStatus());
        assertNotNull(evacuation.getConfirmedAt());
        verify(repository).save(evacuation);
    }

    @Test
    void execute_evacuationNotFound_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        ConfirmCashEvacuationCommand command = new ConfirmCashEvacuationCommand(10L, 999L);

        assertThrows(CashEvacuationNotFoundException.class, () -> handler.execute(command));
    }

    @Test
    void execute_organizationMismatch_throwsNotFoundException() {
        CashEvacuation evacuation = CashEvacuation.create(
                1L, 10L, 20L, 30L, Money.of("10000.00", CurrencyCode.NGN), 40L
        );

        when(repository.findById(1L)).thenReturn(Optional.of(evacuation));

        ConfirmCashEvacuationCommand command = new ConfirmCashEvacuationCommand(999L, 1L);

        assertThrows(CashEvacuationNotFoundException.class, () -> handler.execute(command));
    }
}
