package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.Till;
import com.atlashub.commerce.storefront.domain.valueobject.TillStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.TillJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.TillMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataTillRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TillRepositoryAdapterTest {

    private SpringDataTillRepository springDataRepo;
    private TillMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private TillRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataTillRepository.class);
        mapper = mock(TillMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new TillRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for till sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_till_seq")).thenReturn(200L);
        assertEquals(200L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find active till by outlet id")
    void shouldFindActiveTillByOutletId() {
        Money openingFloat = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Till till = Till.create(200L, 1L, 10L, "Register 1", 50L, openingFloat);
        TillJpa jpa = new TillJpa(200L, 1L, 10L, "Register 1", new BigDecimal("10000.0000"), new BigDecimal("10000.0000"), null, TillStatus.OPEN, ZonedDateTime.now(), null, 50L, null, 0L);

        when(springDataRepo.findByOutletIdAndStatus(10L, TillStatus.OPEN)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(till);

        Optional<Till> result = adapter.findActiveTillByOutletId(10L);

        assertTrue(result.isPresent());
        assertEquals(200L, result.get().getId());
    }

    @Test
    @DisplayName("Should find all tills by outlet id")
    void shouldFindByOutletId() {
        Money openingFloat = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Till till = Till.create(200L, 1L, 10L, "Register 1", 50L, openingFloat);
        TillJpa jpa = new TillJpa(200L, 1L, 10L, "Register 1", new BigDecimal("10000.0000"), new BigDecimal("10000.0000"), null, TillStatus.OPEN, ZonedDateTime.now(), null, 50L, null, 0L);

        when(springDataRepo.findByOutletId(10L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(till);

        List<Till> result = adapter.findByOutletId(10L);

        assertEquals(1, result.size());
    }
}
