package com.atlashub.accounting.ar.application.command;

import com.atlashub.accounting.ar.application.command.RecordReceivablePayment.RecordReceivablePaymentCommand;
import com.atlashub.accounting.ar.application.command.RecordReceivablePayment.RecordReceivablePaymentHandler;
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
class RecordReceivablePaymentHandlerTest {

    @Mock
    private CustomerReceivableRepository receivableRepository;

    @InjectMocks
    private RecordReceivablePaymentHandler handler;

    @Test
    @DisplayName("Should record payment on existing receivable and save")
    void execute_foundReceivable_shouldRecordPaymentAndSave() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L,
                1L,
                200L,
                300L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(20)
        );

        when(receivableRepository.findById(100L)).thenReturn(Optional.of(receivable));

        Money payment = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        RecordReceivablePaymentCommand command = new RecordReceivablePaymentCommand(100L, payment);

        handler.execute(command);

        assertThat(receivable.getStatus()).isEqualTo(ArStatus.PARTIALLY_COLLECTED);
        assertThat(receivable.getPaidAmount()).isEqualTo(payment);
        verify(receivableRepository).save(receivable);
    }

    @Test
    @DisplayName("Should throw ReceivableNotFoundException when receivable not found")
    void execute_notFound_shouldThrowException() {
        when(receivableRepository.findById(999L)).thenReturn(Optional.empty());

        Money payment = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        RecordReceivablePaymentCommand command = new RecordReceivablePaymentCommand(999L, payment);

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(ReceivableNotFoundException.class);
    }
}
