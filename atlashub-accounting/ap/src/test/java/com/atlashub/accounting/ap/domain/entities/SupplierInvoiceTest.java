package com.atlashub.accounting.ap.domain.entities;

import com.atlashub.accounting.ap.domain.events.SupplierInvoiceCreatedEvent;
import com.atlashub.accounting.ap.domain.events.SupplierInvoicePaidEvent;
import com.atlashub.accounting.ap.domain.exceptions.InvalidInvoiceOperationException;
import com.atlashub.accounting.ap.domain.exceptions.InvoiceAlreadyPaidException;
import com.atlashub.accounting.ap.domain.exceptions.InvoiceDisputedException;
import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierInvoiceTest {

    private final Money validAmount = Money.of(new BigDecimal("150000.00"), CurrencyCode.NGN);
    private final LocalDate validDueDate = LocalDate.now().plusDays(30);

    @Test
    @DisplayName("Should create SupplierInvoice with valid attributes and emit SupplierInvoiceCreatedEvent")
    void create_valid_shouldSucceed() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L,
                1L,
                200L,
                300L,
                "INV-2026-001",
                validAmount,
                validDueDate
        );

        assertThat(invoice.getId()).isEqualTo(100L);
        assertThat(invoice.getOrganizationId()).isEqualTo(1L);
        assertThat(invoice.getSupplierId()).isEqualTo(200L);
        assertThat(invoice.getPurchaseOrderId()).isEqualTo(300L);
        assertThat(invoice.getInvoiceNumber()).isEqualTo("INV-2026-001");
        assertThat(invoice.getAmount()).isEqualTo(validAmount);
        assertThat(invoice.getDueDate()).isEqualTo(validDueDate);
        assertThat(invoice.getStatus()).isEqualTo(ApStatus.UNPAID);
        assertThat(invoice.getPaidAt()).isNull();
        assertThat(invoice.getCreatedAt()).isNotNull();

        assertThat(invoice.peekDomainEvents()).hasSize(1);
        assertThat(invoice.peekDomainEvents().getFirst()).isInstanceOf(SupplierInvoiceCreatedEvent.class);
        SupplierInvoiceCreatedEvent event = (SupplierInvoiceCreatedEvent) invoice.peekDomainEvents().getFirst();
        assertThat(event.payload().invoiceId()).isEqualTo(100L);
        assertThat(event.payload().organizationId()).isEqualTo(1L);
        assertThat(event.payload().supplierId()).isEqualTo(200L);
        assertThat(event.payload().invoiceNumber()).isEqualTo("INV-2026-001");
        assertThat(event.payload().amount()).isEqualByComparingTo(new BigDecimal("150000.00"));
        assertThat(event.payload().currency()).isEqualTo("NGN");
        assertThat(event.payload().dueDate()).isEqualTo(validDueDate);
    }

    @Test
    @DisplayName("Should fail creation when required fields are missing or amount is not positive")
    void create_invalidInvariants_shouldThrow() {
        assertThatThrownBy(() -> SupplierInvoice.create(null, 1L, 200L, 300L, "INV-01", validAmount, validDueDate))
                .isInstanceOf(InvalidInvoiceOperationException.class);

        assertThatThrownBy(() -> SupplierInvoice.create(100L, null, 200L, 300L, "INV-01", validAmount, validDueDate))
                .isInstanceOf(InvalidInvoiceOperationException.class);

        assertThatThrownBy(() -> SupplierInvoice.create(100L, 1L, null, 300L, "INV-01", validAmount, validDueDate))
                .isInstanceOf(InvalidInvoiceOperationException.class);

        assertThatThrownBy(() -> SupplierInvoice.create(100L, 1L, 200L, null, "INV-01", validAmount, validDueDate))
                .isInstanceOf(InvalidInvoiceOperationException.class);

        assertThatThrownBy(() -> SupplierInvoice.create(100L, 1L, 200L, 300L, "   ", validAmount, validDueDate))
                .isInstanceOf(InvalidInvoiceOperationException.class);

        Money zeroAmount = Money.of(BigDecimal.ZERO, CurrencyCode.NGN);
        assertThatThrownBy(() -> SupplierInvoice.create(100L, 1L, 200L, 300L, "INV-01", zeroAmount, validDueDate))
                .isInstanceOf(InvalidInvoiceOperationException.class);

        assertThatThrownBy(() -> SupplierInvoice.create(100L, 1L, 200L, 300L, "INV-01", validAmount, null))
                .isInstanceOf(InvalidInvoiceOperationException.class);
    }

    @Test
    @DisplayName("Should mark invoice as paid and register SupplierInvoicePaidEvent")
    void markPaid_whenUnpaid_shouldSucceed() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-2026-001", validAmount, validDueDate
        );
        invoice.pullDomainEvents();

        ZonedDateTime paidAt = ZonedDateTime.now();
        invoice.markPaid("PAYOUT-REF-999", paidAt);

        assertThat(invoice.getStatus()).isEqualTo(ApStatus.PAID);
        assertThat(invoice.getPaidAt()).isEqualTo(paidAt);
        assertThat(invoice.peekDomainEvents()).hasSize(1);
        assertThat(invoice.peekDomainEvents().getFirst()).isInstanceOf(SupplierInvoicePaidEvent.class);

        SupplierInvoicePaidEvent event = (SupplierInvoicePaidEvent) invoice.peekDomainEvents().getFirst();
        assertThat(event.payload().invoiceId()).isEqualTo(100L);
        assertThat(event.payload().organizationId()).isEqualTo(1L);
        assertThat(event.payload().supplierId()).isEqualTo(200L);
        assertThat(event.payload().payoutReference()).isEqualTo("PAYOUT-REF-999");
        assertThat(event.payload().paidAt()).isEqualTo(paidAt);
    }

    @Test
    @DisplayName("Should throw InvoiceAlreadyPaidException when attempting to pay an already paid invoice")
    void markPaid_whenAlreadyPaid_shouldThrow() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-2026-001", validAmount, validDueDate
        );
        invoice.markPaid("PAYOUT-1", ZonedDateTime.now());

        assertThatThrownBy(() -> invoice.markPaid("PAYOUT-2", ZonedDateTime.now()))
                .isInstanceOf(InvoiceAlreadyPaidException.class);
    }

    @Test
    @DisplayName("Should throw InvoiceDisputedException when attempting to pay a disputed invoice")
    void markPaid_whenDisputed_shouldThrow() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-2026-001", validAmount, validDueDate
        );
        invoice.dispute("Wrong item quantities received");

        assertThatThrownBy(() -> invoice.markPaid("PAYOUT-1", ZonedDateTime.now()))
                .isInstanceOf(InvoiceDisputedException.class);
    }

    @Test
    @DisplayName("Should dispute an unpaid invoice successfully")
    void dispute_whenUnpaid_shouldSucceed() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-2026-001", validAmount, validDueDate
        );

        invoice.dispute("Price mismatch on goods");
        assertThat(invoice.getStatus()).isEqualTo(ApStatus.DISPUTED);
    }

    @Test
    @DisplayName("Should fail to dispute an already paid or already disputed invoice")
    void dispute_invalidStates_shouldThrow() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-2026-001", validAmount, validDueDate
        );
        invoice.dispute("Initial dispute");

        assertThatThrownBy(() -> invoice.dispute("Second dispute"))
                .isInstanceOf(InvoiceDisputedException.class);

        SupplierInvoice paidInvoice = SupplierInvoice.create(
                101L, 1L, 200L, 301L, "INV-2026-002", validAmount, validDueDate
        );
        paidInvoice.markPaid("PAYOUT-REF", ZonedDateTime.now());

        assertThatThrownBy(() -> paidInvoice.dispute("Dispute paid"))
                .isInstanceOf(InvalidInvoiceOperationException.class);
    }

    @Test
    @DisplayName("Should resolve dispute and return invoice to UNPAID status")
    void resolveDispute_whenDisputed_shouldSucceed() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-2026-001", validAmount, validDueDate
        );
        invoice.dispute("Pricing issue");
        assertThat(invoice.getStatus()).isEqualTo(ApStatus.DISPUTED);

        invoice.resolveDispute();
        assertThat(invoice.getStatus()).isEqualTo(ApStatus.UNPAID);
    }

    @Test
    @DisplayName("Should fail resolving dispute if invoice was not in dispute")
    void resolveDispute_whenNotDisputed_shouldThrow() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-2026-001", validAmount, validDueDate
        );

        assertThatThrownBy(invoice::resolveDispute)
                .isInstanceOf(InvalidInvoiceOperationException.class);
    }
}
