package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.CustomerCredit;
import com.atlashub.commerce.storefront.domain.valueobject.CreditStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.CustomerCreditJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.CustomerCreditMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataCustomerCreditRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerCreditRepositoryAdapterTest {

    private SpringDataCustomerCreditRepository springDataRepo;
    private CustomerCreditMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private CustomerCreditRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataCustomerCreditRepository.class);
        mapper = mock(CustomerCreditMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new CustomerCreditRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for customer credit sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_customer_credit_seq")).thenReturn(300L);
        assertEquals(300L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find customer credit by organization id and customer id")
    void shouldFindByOrganizationIdAndCustomerId() {
        Money limit = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        CustomerCredit credit = CustomerCredit.create(1L, 10L, 50L, limit);
        CustomerCreditJpa jpa = new CustomerCreditJpa(1L, 10L, 50L, new BigDecimal("50000.0000"), BigDecimal.ZERO, CreditStatus.SETTLED, 0L);

        when(springDataRepo.findByOrganizationIdAndCustomerId(10L, 50L)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(credit);

        Optional<CustomerCredit> result = adapter.findByOrganizationIdAndCustomerId(10L, 50L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }
}
