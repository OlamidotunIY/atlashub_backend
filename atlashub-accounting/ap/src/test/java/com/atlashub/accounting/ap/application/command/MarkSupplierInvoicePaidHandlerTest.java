package com.atlashub.accounting.ap.application.command;

import com.atlashub.accounting.ap.application.command.MarkSupplierInvoicePaid.MarkSupplierInvoicePaidCommand;
import com.atlashub.accounting.ap.application.command.MarkSupplierInvoicePaid.MarkSupplierInvoicePaidHandler;
import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.domain.exceptions.SupplierInvoiceNotFoundException;
import com.atlashub.accounting.ap.domain.repositories.SupplierInvoiceRepository;
import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
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
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkSupplierInvoicePaidHandlerTest {

    @Mock
    private SupplierInvoiceRepository invoiceRepository;

    @InjectMocks
    private MarkSupplierInvoicePaidHandler handler;

    @Test
    @DisplayName("Should mark invoice as paid and persist changes")
    void execute_foundInvoice_shouldMarkPaidAndSave() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L,
                1L,
                200L,
                300L,
                "INV-100",
                Money.of(new BigDecimal("75000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(20)
        );

        when(invoiceRepository.findById(100L)).thenReturn(Optional.of(invoice));

        MarkSupplierInvoicePaidCommand command = new MarkSupplierInvoicePaidCommand(
                100L,
                "PAYOUT-XYZ",
                ZonedDateTime.now()
        );

        handler.execute(command);

        assertThat(invoice.getStatus()).isEqualTo(ApStatus.PAID);
        assertThat(invoice.getPaidAt()).isNotNull();
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should throw SupplierInvoiceNotFoundException when invoice not found")
    void execute_notFound_shouldThrowException() {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        MarkSupplierInvoicePaidCommand command = new MarkSupplierInvoicePaidCommand(
                999L,
                "PAYOUT-XYZ"
        );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(SupplierInvoiceNotFoundException.class);
    }
}
