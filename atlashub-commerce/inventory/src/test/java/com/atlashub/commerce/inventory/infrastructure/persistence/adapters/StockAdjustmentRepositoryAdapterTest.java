package com.atlashub.commerce.inventory.infrastructure.persistence.adapters;

import com.atlashub.commerce.inventory.domain.entities.StockAdjustment;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockAdjustmentJpa;
import com.atlashub.commerce.inventory.infrastructure.persistence.mappers.StockAdjustmentMapper;
import com.atlashub.commerce.inventory.infrastructure.persistence.repositories.SpringDataStockAdjustmentRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StockAdjustmentRepositoryAdapterTest {

    private SpringDataStockAdjustmentRepository springDataRepo;
    private StockAdjustmentMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private StockAdjustmentRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataStockAdjustmentRepository.class);
        mapper = mock(StockAdjustmentMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new StockAdjustmentRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for stock adjustment sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_stock_adjustment_seq")).thenReturn(55L);
        assertEquals(55L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find adjustments by organization and inventory id")
    void shouldFindByInventoryId() {
        StockAdjustmentJpa jpa = mock(StockAdjustmentJpa.class);
        StockAdjustment domain = mock(StockAdjustment.class);

        when(springDataRepo.findByOrganizationIdAndInventoryId(10L, 5L))
                .thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<StockAdjustment> list = adapter.findByOrganizationIdAndInventoryId(10L, 5L);
        assertEquals(1, list.size());
        assertEquals(domain, list.get(0));
    }
}
