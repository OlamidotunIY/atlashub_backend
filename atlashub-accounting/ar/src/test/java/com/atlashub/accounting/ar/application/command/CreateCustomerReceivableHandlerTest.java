package com.atlashub.accounting.ar.application.command;

import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableCommand;
import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableHandler;
import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.repositories.CustomerReceivableRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCustomerReceivableHandlerTest {

    @Mock
    private CustomerReceivableRepository receivableRepository;

    @InjectMocks
    private CreateCustomerReceivableHandler handler;

    @Test
    @DisplayName("Should create and save new receivable when none exists for sales order")
    void execute_newReceivable_shouldCreateAndReturnId() {
        CreateCustomerReceivableCommand command = new CreateCustomerReceivableCommand(
                1L,
                200L,
                300L,
                Money.of(new BigDecimal("75000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(15)
        );

        when(receivableRepository.findByOrganizationIdAndSalesOrderId(1L, 300L))
                .thenReturn(Optional.empty());
        when(receivableRepository.nextIdentity()).thenReturn(999L);
        when(receivableRepository.save(any(CustomerReceivable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Long resultId = handler.execute(command);

        assertThat(resultId).isEqualTo(999L);
        verify(receivableRepository).save(any(CustomerReceivable.class));
    }

    @Test
    @DisplayName("Should return existing receivable ID if already created for sales order")
    void execute_existingReceivable_shouldReturnExistingIdWithoutCreating() {
        CustomerReceivable existing = CustomerReceivable.create(
                888L,
                1L,
                200L,
                300L,
                Money.of(new BigDecimal("75000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(15)
        );

        CreateCustomerReceivableCommand command = new CreateCustomerReceivableCommand(
                1L,
                200L,
                300L,
                Money.of(new BigDecimal("75000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(15)
        );

        when(receivableRepository.findByOrganizationIdAndSalesOrderId(1L, 300L))
                .thenReturn(Optional.of(existing));

        Long resultId = handler.execute(command);

        assertThat(resultId).isEqualTo(888L);
        verify(receivableRepository, never()).nextIdentity();
        verify(receivableRepository, never()).save(any(CustomerReceivable.class));
    }
}
