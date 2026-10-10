package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.Vendor;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.VendorJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.VendorMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataVendorRepository;
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

class VendorRepositoryAdapterTest {

    private SpringDataVendorRepository springDataRepo;
    private VendorMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private VendorRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataVendorRepository.class);
        mapper = mock(VendorMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new VendorRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return nextIdentity using sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_vendor_seq")).thenReturn(88L);
        assertEquals(88L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find vendors by organization")
    void shouldFindByOrganization() {
        VendorJpa jpa = mock(VendorJpa.class);
        Vendor domain = mock(Vendor.class);

        when(springDataRepo.findByOrganizationId(10L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Vendor> result = adapter.findByOrganizationId(10L);
        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }

    @Test
    @DisplayName("Should find vendors by organization and status")
    void shouldFindByOrganizationAndStatus() {
        VendorJpa jpa = mock(VendorJpa.class);
        Vendor domain = mock(Vendor.class);

        when(springDataRepo.findByOrganizationIdAndStatus(10L, VendorStatus.ACTIVE)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Vendor> result = adapter.findByOrganizationIdAndStatus(10L, VendorStatus.ACTIVE);
        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }

    @Test
    @DisplayName("Should find vendor by organization and user id")
    void shouldFindByOrganizationAndUserId() {
        VendorJpa jpa = mock(VendorJpa.class);
        Vendor domain = mock(Vendor.class);

        when(springDataRepo.findByOrganizationIdAndUserId(10L, 999L)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Vendor> result = adapter.findByOrganizationIdAndUserId(10L, 999L);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }
}
