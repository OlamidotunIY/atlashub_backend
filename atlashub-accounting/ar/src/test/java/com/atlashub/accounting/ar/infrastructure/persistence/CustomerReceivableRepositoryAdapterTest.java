package com.atlashub.accounting.ar.infrastructure.persistence;

import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.accounting.ar.infrastructure.persistence.adapters.CustomerReceivableRepositoryAdapter;
import com.atlashub.accounting.ar.infrastructure.persistence.entities.CustomerReceivableJpaEntity;
import com.atlashub.accounting.ar.infrastructure.persistence.mappers.CustomerReceivableMapper;
import com.atlashub.accounting.ar.infrastructure.persistence.repositories.SpringDataCustomerReceivableRepository;
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
class CustomerReceivableRepositoryAdapterTest {

    @Mock
    private SpringDataCustomerReceivableRepository springDataRepository;

    @Mock
    private CustomerReceivableMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private CustomerReceivableRepositoryAdapter adapter;

    private CustomerReceivable receivable;
    private CustomerReceivableJpaEntity jpaEntity;

    @BeforeEach
    void setUp() {
        adapter = new CustomerReceivableRepositoryAdapter(
                springDataRepository, mapper, sequenceGenerator, eventPublisher
        );

        receivable = CustomerReceivable.create(
                100L, 1L, 200L, 300L,
                Money.of(new BigDecimal("80000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(30)
        );

        jpaEntity = new CustomerReceivableJpaEntity(
                100L, 1L, 200L, 300L,
                new BigDecimal("80000.00"), BigDecimal.ZERO, "NGN",
                LocalDate.now().plusDays(30),
                ArStatus.OUTSTANDING, ZonedDateTime.now(), 0L
        );
    }

    @Test
    @DisplayName("Should return correct sequence name")
    void getSequenceName_shouldReturnCorrectName() {
        String seqName = (String) ReflectionTestUtils.invokeMethod(adapter, "getSequenceName");
        assertThat(seqName).isEqualTo("accounting_customer_receivable_seq");
    }

    @Test
    @DisplayName("Should find receivable by organization ID and sales order ID")
    void findByOrganizationIdAndSalesOrderId_shouldMapAndReturn() {
        when(springDataRepository.findByOrganizationIdAndSalesOrderId(1L, 300L))
                .thenReturn(Optional.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(receivable);

        Optional<CustomerReceivable> result = adapter.findByOrganizationIdAndSalesOrderId(1L, 300L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(100L);
        verify(springDataRepository).findByOrganizationIdAndSalesOrderId(1L, 300L);
    }

    @Test
    @DisplayName("Should find receivables by organization ID and customer ID")
    void findByOrganizationIdAndCustomerId_shouldMapAndReturnList() {
        when(springDataRepository.findByOrganizationIdAndCustomerId(1L, 200L))
                .thenReturn(List.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(receivable);

        List<CustomerReceivable> results = adapter.findByOrganizationIdAndCustomerId(1L, 200L);

        assertThat(results).hasSize(1);
        verify(springDataRepository).findByOrganizationIdAndCustomerId(1L, 200L);
    }

    @Test
    @DisplayName("Should find receivables by organization ID and status")
    void findByOrganizationIdAndStatus_shouldMapAndReturnList() {
        when(springDataRepository.findByOrganizationIdAndStatus(1L, ArStatus.OUTSTANDING))
                .thenReturn(List.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(receivable);

        List<CustomerReceivable> results = adapter.findByOrganizationIdAndStatus(1L, ArStatus.OUTSTANDING);

        assertThat(results).hasSize(1);
        verify(springDataRepository).findByOrganizationIdAndStatus(1L, ArStatus.OUTSTANDING);
    }

    @Test
    @DisplayName("Should find receivables by organization ID")
    void findByOrganizationId_shouldMapAndReturnList() {
        when(springDataRepository.findByOrganizationId(1L))
                .thenReturn(List.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(receivable);

        List<CustomerReceivable> results = adapter.findByOrganizationId(1L);

        assertThat(results).hasSize(1);
        verify(springDataRepository).findByOrganizationId(1L);
    }
}
