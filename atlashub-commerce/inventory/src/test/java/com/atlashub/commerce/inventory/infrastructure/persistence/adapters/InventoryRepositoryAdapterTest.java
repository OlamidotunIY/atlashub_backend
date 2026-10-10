package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.InventoryJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.InventoryMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataInventoryRepository;
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

class InventoryRepositoryAdapterTest {

    private SpringDataInventoryRepository springDataRepo;
    private InventoryMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private InventoryRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataInventoryRepository.class);
        mapper = mock(InventoryMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new InventoryRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for inventory sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_inventory_seq")).thenReturn(100L);
        assertEquals(100L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find inventory by organization, outlet and product")
    void shouldFindByProduct() {
        InventoryJpa jpa = mock(InventoryJpa.class);
        Inventory domain = mock(Inventory.class);

        when(springDataRepo.findByOrganizationIdAndOutletIdAndProductId(10L, 20L, 30L))
                .thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Inventory> result = adapter.findByOrganizationIdAndOutletIdAndProductId(10L, 20L, 30L);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find inventory with variant")
    void shouldFindByVariant() {
        InventoryJpa jpa = mock(InventoryJpa.class);
        Inventory domain = mock(Inventory.class);

        when(springDataRepo.findByOrganizationIdAndOutletIdAndProductIdAndVariantId(10L, 20L, 30L, 40L))
                .thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Inventory> result = adapter.findByOrganizationIdAndOutletIdAndProductIdAndVariantId(10L, 20L, 30L, 40L);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find inventories by outlet")
    void shouldFindByOutlet() {
        InventoryJpa jpa = mock(InventoryJpa.class);
        Inventory domain = mock(Inventory.class);

        when(springDataRepo.findByOrganizationIdAndOutletId(10L, 20L))
                .thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Inventory> list = adapter.findByOrganizationIdAndOutletId(10L, 20L);
        assertEquals(1, list.size());
        assertEquals(domain, list.get(0));
    }

    @Test
    @DisplayName("Should find low stock inventories")
    void shouldFindLowStock() {
        InventoryJpa jpa = mock(InventoryJpa.class);
        Inventory domain = mock(Inventory.class);

        when(springDataRepo.findLowStock(10L, 20L))
                .thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Inventory> list = adapter.findLowStock(10L, 20L);
        assertEquals(1, list.size());
        assertEquals(domain, list.get(0));
    }
}
