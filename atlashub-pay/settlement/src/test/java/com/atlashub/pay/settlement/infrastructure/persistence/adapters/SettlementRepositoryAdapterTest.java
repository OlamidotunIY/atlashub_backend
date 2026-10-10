package com.atlashub.pay.settlement.infrastructure.persistence.adapters;

import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementJpa;
import com.atlashub.pay.settlement.infrastructure.persistence.mappers.SettlementMapper;
import com.atlashub.pay.settlement.infrastructure.persistence.repositories.SpringDataSettlementRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementRepositoryAdapterTest {

    @Mock
    private SpringDataSettlementRepository springDataRepo;

    @Mock
    private SettlementMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private SettlementRepositoryAdapter adapter;

    @Test
    @DisplayName("Should return pay_settlement_seq as sequence name")
    void shouldReturnCorrectSequenceName() {
        assertEquals("pay_settlement_seq", adapter.getSequenceName());
    }

    @Test
    @DisplayName("Should generate next identity using sequence generator")
    void shouldGenerateNextIdentity() {
        when(sequenceGenerator.nextIdentity("pay_settlement_seq")).thenReturn(12345L);

        Long identity = adapter.nextIdentity();

        assertEquals(12345L, identity);
        verify(sequenceGenerator).nextIdentity("pay_settlement_seq");
    }

    @Test
    @DisplayName("Should find settlement by provider settlement ID when present")
    void shouldFindByProviderSettlementIdWhenPresent() {
        String providerSettlementId = "PROV-SETTLE-001";
        SettlementJpa jpa = mock(SettlementJpa.class);
        Settlement domain = mock(Settlement.class);

        when(springDataRepo.findByProviderAndEnvironmentAndProviderSettlementId(
                PaymentProvider.PAYSTACK, ApiEnvironment.LIVE, providerSettlementId)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Settlement> result = adapter.findByProviderAndEnvironmentAndProviderSettlementId(
                PaymentProvider.PAYSTACK, ApiEnvironment.LIVE, providerSettlementId);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
        verify(springDataRepo).findByProviderAndEnvironmentAndProviderSettlementId(
                PaymentProvider.PAYSTACK, ApiEnvironment.LIVE, providerSettlementId);
        verify(mapper).toDomain(jpa);
    }

    @Test
    @DisplayName("Should return empty optional when provider settlement ID is not found")
    void shouldReturnEmptyWhenProviderSettlementIdNotFound() {
        String providerSettlementId = "NON-EXISTENT";

        when(springDataRepo.findByProviderAndEnvironmentAndProviderSettlementId(
                PaymentProvider.PAYSTACK, ApiEnvironment.LIVE, providerSettlementId)).thenReturn(Optional.empty());

        Optional<Settlement> result = adapter.findByProviderAndEnvironmentAndProviderSettlementId(
                PaymentProvider.PAYSTACK, ApiEnvironment.LIVE, providerSettlementId);

        assertFalse(result.isPresent());
        verify(springDataRepo).findByProviderAndEnvironmentAndProviderSettlementId(
                PaymentProvider.PAYSTACK, ApiEnvironment.LIVE, providerSettlementId);
    }

    @Test
    @DisplayName("Should search settlements with pagination and map to PageResult")
    void shouldFindByOrganizationIdWithPagination() {
        Long organizationId = 1L;
        SettlementStatus status = SettlementStatus.CONFIRMED;
        ZonedDateTime dateFrom = ZonedDateTime.now().minusDays(7);
        ZonedDateTime dateTo = ZonedDateTime.now();
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        SettlementJpa jpa = mock(SettlementJpa.class);
        Settlement domain = mock(Settlement.class);
        Page<SettlementJpa> jpaPage = new PageImpl<>(List.of(jpa), pageable, 1);

        when(springDataRepo.searchSettlements(organizationId, ApiEnvironment.LIVE, status, dateFrom, dateTo, pageable))
                .thenReturn(jpaPage);
        when(mapper.toDomain(jpa)).thenReturn(domain);

        PageResult<Settlement> result = adapter.findByOrganizationId(
                organizationId, ApiEnvironment.LIVE, status, dateFrom, dateTo, page, size
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(domain, result.content().get(0));
        assertEquals(0, result.pageNumber());
        assertEquals(10, result.pageSize());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
        verify(springDataRepo).searchSettlements(organizationId, ApiEnvironment.LIVE, status, dateFrom, dateTo, pageable);
    }

    @Test
    @DisplayName("Should find settlement by ID")
    void shouldFindById() {
        Long id = 99L;
        SettlementJpa jpa = mock(SettlementJpa.class);
        Settlement domain = mock(Settlement.class);

        when(springDataRepo.findById(id)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Settlement> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
        verify(springDataRepo).findById(id);
    }

    @Test
    @DisplayName("Should save new settlement and publish events")
    void shouldSaveNewSettlement() {
        ZonedDateTime now = ZonedDateTime.now();
        Settlement settlement = Settlement.create(
                10L, 1L, PaymentProvider.PAYSTACK, "SETTLE-NEW",
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("980.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("20.00"), CurrencyCode.NGN),
                50L, now, "New settlement"
        );
        SettlementJpa jpa = mock(SettlementJpa.class);

        when(springDataRepo.findById(10L)).thenReturn(Optional.empty());
        when(mapper.toPersistence(settlement)).thenReturn(jpa);
        when(springDataRepo.save(jpa)).thenReturn(jpa);
        when(mapper.toDomain(jpa)).thenReturn(settlement);

        Settlement saved = adapter.save(settlement);

        assertNotNull(saved);
        assertEquals(settlement, saved);
        verify(springDataRepo).save(jpa);
    }
}
