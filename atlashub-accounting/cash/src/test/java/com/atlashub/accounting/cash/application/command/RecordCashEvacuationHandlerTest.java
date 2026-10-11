package com.atlashub.accounting.cash.application.command;

import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationCommand;
import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationHandler;
import com.atlashub.accounting.cash.domain.entities.CashEvacuation;
import com.atlashub.accounting.cash.domain.repositories.CashEvacuationRepository;
import com.atlashub.accounting.cash.domain.valueobject.EvacuationStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordCashEvacuationHandlerTest {

    private CashEvacuationRepository repository;
    private RecordCashEvacuationHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(CashEvacuationRepository.class);
        handler = new RecordCashEvacuationHandler(repository);
    }

    @Test
    void execute_createsAndSavesCashEvacuation() {
        when(repository.nextIdentity()).thenReturn(100L);
        when(repository.save(any(CashEvacuation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordCashEvacuationCommand command = new RecordCashEvacuationCommand(
                10L, 20L, 30L, Money.of("25000.00", CurrencyCode.NGN), 40L
        );

        Long resultId = handler.execute(command);

        assertEquals(100L, resultId);

        ArgumentCaptor<CashEvacuation> captor = ArgumentCaptor.forClass(CashEvacuation.class);
        verify(repository).save(captor.capture());

        CashEvacuation saved = captor.getValue();
        assertEquals(100L, saved.getId());
        assertEquals(10L, saved.getOrganizationId());
        assertEquals(20L, saved.getOutletId());
        assertEquals(30L, saved.getTillId());
        assertEquals(Money.of("25000.00", CurrencyCode.NGN), saved.getAmount());
        assertEquals(40L, saved.getEvacuatedBy());
        assertEquals(EvacuationStatus.PENDING, saved.getStatus());
    }
}
