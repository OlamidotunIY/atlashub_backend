package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.Discount;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.DiscountJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.DiscountMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataDiscountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscountRepositoryAdapterTest {

    private SpringDataDiscountRepository springDataRepo;
    private DiscountMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private DiscountRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataDiscountRepository.class);
        mapper = mock(DiscountMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new DiscountRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return nextIdentity using sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_discount_seq")).thenReturn(123L);
        assertEquals(123L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find discount by organization and name")
    void shouldFindByOrganizationAndName() {
        DiscountJpa jpa = mock(DiscountJpa.class);
        Discount domain = mock(Discount.class);

        when(springDataRepo.findByOrganizationIdAndName(10L, "SALE")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Discount> result = adapter.findByOrganizationIdAndName(10L, "SALE");
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find active discounts for organization")
    void shouldFindActiveByOrganization() {
        DiscountJpa jpa = mock(DiscountJpa.class);
        Discount domain = mock(Discount.class);

        when(springDataRepo.findByOrganizationIdAndActiveTrue(10L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Discount> result = adapter.findActiveByOrganizationId(10L);
        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }
}
