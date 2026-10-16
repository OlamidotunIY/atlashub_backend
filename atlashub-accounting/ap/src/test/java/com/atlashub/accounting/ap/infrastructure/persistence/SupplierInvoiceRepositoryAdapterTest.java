package com.atlashub.accounting.ap.infrastructure.persistence;

import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
import com.atlashub.accounting.ap.infrastructure.persistence.adapters.SupplierInvoiceRepositoryAdapter;
import com.atlashub.accounting.ap.infrastructure.persistence.entities.SupplierInvoiceJpaEntity;
import com.atlashub.accounting.ap.infrastructure.persistence.mappers.SupplierInvoiceMapper;
import com.atlashub.accounting.ap.infrastructure.persistence.repositories.SpringDataSupplierInvoiceRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierInvoiceRepositoryAdapterTest {

    @Mock
    private SpringDataSupplierInvoiceRepository springDataRepository;

    @Mock
    private SupplierInvoiceMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private SupplierInvoiceRepositoryAdapter adapter;

    private SupplierInvoice invoice;
    private SupplierInvoiceJpaEntity jpaEntity;

    @BeforeEach
    void setUp() {
        adapter = new SupplierInvoiceRepositoryAdapter(
                springDataRepository, mapper, sequenceGenerator, eventPublisher
        );

        invoice = SupplierInvoice.create(
                100L, 1L, 200L, 300L, "INV-100",
                Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(30)
        );

        jpaEntity = new SupplierInvoiceJpaEntity(
                100L, 1L, 200L, 300L, "INV-100",
                new BigDecimal("50000.00"), "NGN",
                LocalDate.now().plusDays(30),
                ApStatus.UNPAID, null, ZonedDateTime.now(), 0L
        );
    }

    @Test
    @DisplayName("Should return correct sequence name")
    void getSequenceName_shouldReturnCorrectName() {
        String seqName = (String) ReflectionTestUtils.invokeMethod(adapter, "getSequenceName");
        assertThat(seqName).isEqualTo("accounting_supplier_invoice_seq");
    }

    @Test
    @DisplayName("Should find invoice by organization ID and purchase order ID")
    void findByOrganizationIdAndPurchaseOrderId_shouldMapAndReturn() {
        when(springDataRepository.findByOrganizationIdAndPurchaseOrderId(1L, 300L))
                .thenReturn(Optional.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(invoice);

        Optional<SupplierInvoice> result = adapter.findByOrganizationIdAndPurchaseOrderId(1L, 300L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(100L);
        verify(springDataRepository).findByOrganizationIdAndPurchaseOrderId(1L, 300L);
    }

    @Test
    @DisplayName("Should find invoices by organization ID and supplier ID")
    void findByOrganizationIdAndSupplierId_shouldMapAndReturnList() {
        when(springDataRepository.findByOrganizationIdAndSupplierId(1L, 200L))
                .thenReturn(List.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(invoice);

        List<SupplierInvoice> results = adapter.findByOrganizationIdAndSupplierId(1L, 200L);

        assertThat(results).hasSize(1);
        verify(springDataRepository).findByOrganizationIdAndSupplierId(1L, 200L);
    }

    @Test
    @DisplayName("Should find invoices by organization ID and status")
    void findByOrganizationIdAndStatus_shouldMapAndReturnList() {
        when(springDataRepository.findByOrganizationIdAndStatus(1L, ApStatus.UNPAID))
                .thenReturn(List.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(invoice);

        List<SupplierInvoice> results = adapter.findByOrganizationIdAndStatus(1L, ApStatus.UNPAID);

        assertThat(results).hasSize(1);
        verify(springDataRepository).findByOrganizationIdAndStatus(1L, ApStatus.UNPAID);
    }

    @Test
    @DisplayName("Should find invoices by organization ID")
    void findByOrganizationId_shouldMapAndReturnList() {
        when(springDataRepository.findByOrganizationId(1L))
                .thenReturn(List.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(invoice);

        List<SupplierInvoice> results = adapter.findByOrganizationId(1L);

        assertThat(results).hasSize(1);
        verify(springDataRepository).findByOrganizationId(1L);
    }
}
