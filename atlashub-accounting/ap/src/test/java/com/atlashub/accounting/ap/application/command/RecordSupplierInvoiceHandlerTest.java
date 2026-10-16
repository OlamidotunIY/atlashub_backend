package com.atlashub.accounting.ap.application.command;

import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceCommand;
import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceHandler;
import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.domain.repositories.SupplierInvoiceRepository;
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
class RecordSupplierInvoiceHandlerTest {

    @Mock
    private SupplierInvoiceRepository invoiceRepository;

    @InjectMocks
    private RecordSupplierInvoiceHandler handler;

    @Test
    @DisplayName("Should create and save new invoice when no invoice exists for purchase order")
    void execute_newInvoice_shouldCreateAndReturnId() {
        RecordSupplierInvoiceCommand command = new RecordSupplierInvoiceCommand(
                1L,
                200L,
                300L,
                "INV-100",
                Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(15)
        );

        when(invoiceRepository.findByOrganizationIdAndPurchaseOrderId(1L, 300L))
                .thenReturn(Optional.empty());
        when(invoiceRepository.nextIdentity()).thenReturn(999L);
        when(invoiceRepository.save(any(SupplierInvoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Long resultId = handler.execute(command);

        assertThat(resultId).isEqualTo(999L);
        verify(invoiceRepository).save(any(SupplierInvoice.class));
    }

    @Test
    @DisplayName("Should return existing invoice ID if already recorded for purchase order")
    void execute_existingInvoice_shouldReturnExistingIdWithoutCreating() {
        SupplierInvoice existingInvoice = SupplierInvoice.create(
                888L,
                1L,
                200L,
                300L,
                "INV-100",
                Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(15)
        );

        RecordSupplierInvoiceCommand command = new RecordSupplierInvoiceCommand(
                1L,
                200L,
                300L,
                "INV-100",
                Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(15)
        );

        when(invoiceRepository.findByOrganizationIdAndPurchaseOrderId(1L, 300L))
                .thenReturn(Optional.of(existingInvoice));

        Long resultId = handler.execute(command);

        assertThat(resultId).isEqualTo(888L);
        verify(invoiceRepository, never()).nextIdentity();
        verify(invoiceRepository, never()).save(any(SupplierInvoice.class));
    }
}
