package com.atlashub.billing.domain.entities;

import com.atlashub.billing.domain.exceptions.BillingDomainException;
import com.atlashub.billing.domain.exceptions.InvoiceAlreadyPaidException;
import com.atlashub.billing.domain.exceptions.InvoiceVoidedException;
import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillingInvoiceTest {

    @Test
    void create_validInvoice_setsDraftStatus() {
        BillingInvoice invoice = BillingInvoice.create(
                200L,
                50L,
                100L,
                "INV-2026-00001",
                "UNIVERSAL_SUBSCRIPTION",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(3750),
                "NGN",
                LocalDate.now().plusDays(7)
        );

        assertEquals(200L, invoice.getId());
        assertEquals("INV-2026-00001", invoice.getInvoiceNumber());
        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
        assertEquals(BigDecimal.valueOf(53750), invoice.getTotalAmount());
    }

    @Test
    void create_missingRequiredFields_throwsException() {
        assertThrows(BillingDomainException.class, () -> BillingInvoice.create(
                null, 50L, 100L, "INV-1", "CODE",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(100), BigDecimal.ZERO, "NGN", LocalDate.now().plusDays(7)
        ));

        assertThrows(BillingDomainException.class, () -> BillingInvoice.create(
                200L, null, 100L, "INV-1", "CODE",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(100), BigDecimal.ZERO, "NGN", LocalDate.now().plusDays(7)
        ));

        assertThrows(BillingDomainException.class, () -> BillingInvoice.create(
                200L, 50L, 100L, "", "CODE",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(100), BigDecimal.ZERO, "NGN", LocalDate.now().plusDays(7)
        ));
    }

    @Test
    void issue_transitionsFromDraftToIssued() {
        BillingInvoice invoice = BillingInvoice.create(
                200L, 50L, 100L, "INV-2026-00001", "UNIVERSAL_SUBSCRIPTION",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(3750), "NGN", LocalDate.now().plusDays(7)
        );

        invoice.issue();

        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
        assertNotNull(invoice.getIssuedAt());
        assertEquals(1, invoice.pullDomainEvents().size());
    }

    @Test
    void markPaid_transitionsToPaid() {
        BillingInvoice invoice = BillingInvoice.create(
                200L, 50L, 100L, "INV-2026-00001", "UNIVERSAL_SUBSCRIPTION",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(3750), "NGN", LocalDate.now().plusDays(7)
        );
        invoice.issue();

        invoice.markPaid("PAY_REF_12345", ZonedDateTime.now());

        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals("PAY_REF_12345", invoice.getPaymentReference());
        assertNotNull(invoice.getPaidAt());
    }

    @Test
    void markPaid_whenAlreadyPaid_throwsException() {
        BillingInvoice invoice = BillingInvoice.create(
                200L, 50L, 100L, "INV-2026-00001", "UNIVERSAL_SUBSCRIPTION",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(3750), "NGN", LocalDate.now().plusDays(7)
        );
        invoice.issue();
        invoice.markPaid("PAY_REF_12345", ZonedDateTime.now());

        assertThrows(InvoiceAlreadyPaidException.class, () ->
                invoice.markPaid("PAY_REF_67890", ZonedDateTime.now())
        );
    }

    @Test
    void markOverdue_transitionsToOverdue() {
        BillingInvoice invoice = BillingInvoice.create(
                200L, 50L, 100L, "INV-2026-00001", "UNIVERSAL_SUBSCRIPTION",
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(3),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(3750), "NGN", LocalDate.now().minusDays(1)
        );
        invoice.issue();

        invoice.markOverdue();

        assertEquals(InvoiceStatus.OVERDUE, invoice.getStatus());
    }

    @Test
    void voidInvoice_transitionsToVoid() {
        BillingInvoice invoice = BillingInvoice.create(
                200L, 50L, 100L, "INV-2026-00001", "UNIVERSAL_SUBSCRIPTION",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(3750), "NGN", LocalDate.now().plusDays(7)
        );
        invoice.issue();

        invoice.voidInvoice("Correction needed");

        assertEquals(InvoiceStatus.VOID, invoice.getStatus());

        assertThrows(InvoiceVoidedException.class, () ->
                invoice.markPaid("PAY_REF_123", ZonedDateTime.now())
        );
    }
}
