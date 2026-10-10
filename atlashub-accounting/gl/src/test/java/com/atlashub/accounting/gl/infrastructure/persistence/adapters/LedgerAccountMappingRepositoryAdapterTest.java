package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.domain.entities.LedgerAccountMapping;
import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.LedgerAccountMappingJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.LedgerAccountMappingMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataLedgerAccountMappingRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LedgerAccountMappingRepositoryAdapterTest {

    private SpringDataLedgerAccountMappingRepository springDataRepo;
    private LedgerAccountMappingMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private LedgerAccountMappingRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataLedgerAccountMappingRepository.class);
        mapper = mock(LedgerAccountMappingMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new LedgerAccountMappingRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    void nextIdentity_delegatesToSequenceGenerator() {
        when(sequenceGenerator.nextIdentity("accounting_ledger_mapping_seq")).thenReturn(201L);

        Long nextId = adapter.nextIdentity();

        assertEquals(201L, nextId);
        verify(sequenceGenerator).nextIdentity("accounting_ledger_mapping_seq");
    }

    @Test
    void findByOrganizationIdAndSourceSystem_mapsToDomain() {
        LedgerAccountMappingJpa jpa = new LedgerAccountMappingJpa(
                1L, 10L, SourceSystem.COMMERCE_CHECKOUT, 100L, 200L, "Checkout", 0L);
        LedgerAccountMapping domain = LedgerAccountMapping.create(
                1L, 10L, SourceSystem.COMMERCE_CHECKOUT, 100L, 200L, "Checkout");

        when(springDataRepo.findByOrganizationIdAndSourceSystem(10L, SourceSystem.COMMERCE_CHECKOUT))
                .thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<LedgerAccountMapping> result = adapter.findByOrganizationIdAndSourceSystem(10L, SourceSystem.COMMERCE_CHECKOUT);

        assertTrue(result.isPresent());
        assertEquals(SourceSystem.COMMERCE_CHECKOUT, result.get().getSourceSystem());
        verify(springDataRepo).findByOrganizationIdAndSourceSystem(10L, SourceSystem.COMMERCE_CHECKOUT);
    }

    @Test
    void findByOrganizationId_mapsList() {
        LedgerAccountMappingJpa jpa = new LedgerAccountMappingJpa(
                1L, 10L, SourceSystem.COMMERCE_CHECKOUT, 100L, 200L, "Checkout", 0L);
        LedgerAccountMapping domain = LedgerAccountMapping.create(
                1L, 10L, SourceSystem.COMMERCE_CHECKOUT, 100L, 200L, "Checkout");

        when(springDataRepo.findByOrganizationId(10L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<LedgerAccountMapping> result = adapter.findByOrganizationId(10L);

        assertEquals(1, result.size());
        assertEquals("Checkout", result.get(0).getDescription());
    }
}
