package com.atlashub.accounting.ap.application.query;

import com.atlashub.accounting.ap.application.query.ListSupplierInvoices.ListSupplierInvoicesHandler;
import com.atlashub.accounting.ap.application.query.ListSupplierInvoices.ListSupplierInvoicesQuery;
import com.atlashub.accounting.ap.application.query.ListSupplierInvoices.SupplierInvoiceResult;
import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListSupplierInvoicesHandlerTest {

    @Mock
    private SupplierInvoiceRepository invoiceRepository;

    @InjectMocks
    private ListSupplierInvoicesHandler handler;

    private final Money amount = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);

    @Test
    @DisplayName("Should query by status when status filter is provided")
    void execute_withStatusFilter_shouldDelegateToFindByOrganizationIdAndStatus() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-100", amount, LocalDate.now().plusDays(10)
        );

        when(invoiceRepository.findByOrganizationIdAndStatus(1L, ApStatus.UNPAID))
                .thenReturn(List.of(invoice));

        ListSupplierInvoicesQuery query = new ListSupplierInvoicesQuery(1L, null, ApStatus.UNPAID);
        List<SupplierInvoiceResult> results = handler.execute(query);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().id()).isEqualTo(100L);
        assertThat(results.getFirst().status()).isEqualTo(ApStatus.UNPAID);
        verify(invoiceRepository).findByOrganizationIdAndStatus(1L, ApStatus.UNPAID);
    }

    @Test
    @DisplayName("Should query by supplier ID when supplierId filter is provided without status")
    void execute_withSupplierIdFilter_shouldDelegateToFindByOrganizationIdAndSupplierId() {
        SupplierInvoice invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-100", amount, LocalDate.now().plusDays(10)
        );

        when(invoiceRepository.findByOrganizationIdAndSupplierId(1L, 200L))
                .thenReturn(List.of(invoice));

        ListSupplierInvoicesQuery query = new ListSupplierInvoicesQuery(1L, 200L, null);
        List<SupplierInvoiceResult> results = handler.execute(query);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().supplierId()).isEqualTo(200L);
        verify(invoiceRepository).findByOrganizationIdAndSupplierId(1L, 200L);
    }

    @Test
    @DisplayName("Should query by organization ID when no specific filter is provided")
    void execute_withoutSpecificFilters_shouldDelegateToFindByOrganizationId() {
        SupplierInvoice invoice1 = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-100", amount, LocalDate.now().plusDays(10)
        );
        SupplierInvoice invoice2 = SupplierInvoice.create(
                101L, 1L, 201L, 301L, "INV-101", amount, LocalDate.now().plusDays(15)
        );

        when(invoiceRepository.findByOrganizationId(1L))
                .thenReturn(List.of(invoice1, invoice2));

        ListSupplierInvoicesQuery query = new ListSupplierInvoicesQuery(1L, null, null);
        List<SupplierInvoiceResult> results = handler.execute(query);

        assertThat(results).hasSize(2);
        verify(invoiceRepository).findByOrganizationId(1L);
    }
}
