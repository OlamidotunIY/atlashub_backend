package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.Supplier;
import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.SupplierJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.SupplierMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataSupplierRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SupplierRepositoryAdapterTest {

    private SpringDataSupplierRepository springDataRepo;
    private SupplierMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private SupplierRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataSupplierRepository.class);
        mapper = mock(SupplierMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new SupplierRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return nextIdentity using sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_supplier_seq")).thenReturn(55L);
        assertEquals(55L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find suppliers by organization")
    void shouldFindByOrganization() {
        SupplierJpa jpa = mock(SupplierJpa.class);
        Supplier domain = mock(Supplier.class);

        when(springDataRepo.findByOrganizationId(10L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Supplier> result = adapter.findByOrganizationId(10L);
        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }

    @Test
    @DisplayName("Should find suppliers by organization and status")
    void shouldFindByOrganizationAndStatus() {
        SupplierJpa jpa = mock(SupplierJpa.class);
        Supplier domain = mock(Supplier.class);

        when(springDataRepo.findByOrganizationIdAndStatus(10L, SupplierStatus.ACTIVE)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Supplier> result = adapter.findByOrganizationIdAndStatus(10L, SupplierStatus.ACTIVE);
        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }
}
