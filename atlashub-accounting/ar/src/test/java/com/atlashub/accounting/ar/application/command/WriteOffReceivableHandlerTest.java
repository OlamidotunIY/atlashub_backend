package com.atlashub.accounting.ar.application.command;

import com.atlashub.accounting.ar.application.command.WriteOffReceivable.WriteOffReceivableCommand;
import com.atlashub.accounting.ar.application.command.WriteOffReceivable.WriteOffReceivableHandler;
import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.exceptions.ReceivableNotFoundException;
import com.atlashub.accounting.ar.domain.repositories.CustomerReceivableRepository;
import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WriteOffReceivableHandlerTest {

    @Mock
    private CustomerReceivableRepository receivableRepository;

    @InjectMocks
    private WriteOffReceivableHandler handler;

    @Test
    @DisplayName("Should write off receivable and save")
    void execute_foundReceivable_shouldWriteOffAndSave() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L,
                1L,
                200L,
                300L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(20)
        );

        when(receivableRepository.findById(100L)).thenReturn(Optional.of(receivable));

        WriteOffReceivableCommand command = new WriteOffReceivableCommand(100L, "Customer defaulted");

        handler.execute(command);

        assertThat(receivable.getStatus()).isEqualTo(ArStatus.WRITTEN_OFF);
        verify(receivableRepository).save(receivable);
    }

    @Test
    @DisplayName("Should throw ReceivableNotFoundException when receivable not found")
    void execute_notFound_shouldThrowException() {
        when(receivableRepository.findById(999L)).thenReturn(Optional.empty());

        WriteOffReceivableCommand command = new WriteOffReceivableCommand(999L, "Reason");

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(ReceivableNotFoundException.class);
    }
}
