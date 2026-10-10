package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.CustomerDeposit;
import com.atlashub.commerce.storefront.domain.valueobject.DepositStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.CustomerDepositJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.CustomerDepositMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataCustomerDepositRepository;
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

class CustomerDepositRepositoryAdapterTest {

    private SpringDataCustomerDepositRepository springDataRepo;
    private CustomerDepositMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private CustomerDepositRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataCustomerDepositRepository.class);
        mapper = mock(CustomerDepositMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new CustomerDepositRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for customer deposit sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_customer_deposit_seq")).thenReturn(400L);
        assertEquals(400L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find customer deposit by sales order id")
    void shouldFindBySalesOrderId() {
        Money initial = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Money total = Money.of(new BigDecimal("30000.00"), CurrencyCode.NGN);
        CustomerDeposit deposit = CustomerDeposit.create(1L, 100L, initial, total);
        CustomerDepositJpa jpa = new CustomerDepositJpa(1L, 100L, new BigDecimal("10000.0000"), new BigDecimal("20000.0000"), DepositStatus.ACTIVE, 0L);

        when(springDataRepo.findBySalesOrderId(100L)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(deposit);

        Optional<CustomerDeposit> result = adapter.findBySalesOrderId(100L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }
}
