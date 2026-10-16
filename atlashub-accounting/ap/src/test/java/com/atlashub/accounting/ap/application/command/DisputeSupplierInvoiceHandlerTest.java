package com.atlashub.accounting.ap.application.command;

import com.atlashub.accounting.ap.application.command.DisputeSupplierInvoice.DisputeSupplierInvoiceCommand;
import com.atlashub.accounting.ap.application.command.DisputeSupplierInvoice.DisputeSupplierInvoiceHandler;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisputeSupplierInvoiceHandlerTest {

    @Mock
    private SupplierInvoiceRepository invoiceRepository;

    @InjectMocks
    private DisputeSupplierInvoiceHandler handler;

    @Test
    @DisplayName("Should dispute invoice and save when found")
    void execute_foundInvoice_shouldDisputeAndSave() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L,
                1L,
                200L,
                300L,
                "INV-100",
                Money.of(new BigDecimal("30000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(10)
        );

        when(invoiceRepository.findById(100L)).thenReturn(Optional.of(invoice));

        DisputeSupplierInvoiceCommand command = new DisputeSupplierInvoiceCommand(100L, "Damaged goods received");

        handler.execute(command);

        assertThat(invoice.getStatus()).isEqualTo(ApStatus.DISPUTED);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should throw SupplierInvoiceNotFoundException when invoice not found")
    void execute_notFound_shouldThrowException() {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        DisputeSupplierInvoiceCommand command = new DisputeSupplierInvoiceCommand(999L, "Reason");

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(SupplierInvoiceNotFoundException.class);
    }
}
