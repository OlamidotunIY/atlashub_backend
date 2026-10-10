package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.HospitalityTable;
import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.HospitalityTableJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.HospitalityTableMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataHospitalityTableRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HospitalityTableRepositoryAdapterTest {

    private SpringDataHospitalityTableRepository springDataRepo;
    private HospitalityTableMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private HospitalityTableRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataHospitalityTableRepository.class);
        mapper = mock(HospitalityTableMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new HospitalityTableRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return next identity for hospitality table sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_hospitality_table_seq")).thenReturn(50L);
        assertEquals(50L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find table by outlet id and table number")
    void shouldFindByOutletIdAndTableNumber() {
        HospitalityTable table = HospitalityTable.create(1L, 10L, 5L, "Table 1", 4);
        HospitalityTableJpa jpa = new HospitalityTableJpa(1L, 10L, 5L, "Table 1", 4, TableStatus.AVAILABLE, null, 0L);

        when(springDataRepo.findByOutletIdAndTableNumber(5L, "Table 1")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(table);

        Optional<HospitalityTable> result = adapter.findByOutletIdAndTableNumber(5L, "Table 1");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("Should find tables by outlet id")
    void shouldFindByOutletId() {
        HospitalityTable table = HospitalityTable.create(1L, 10L, 5L, "Table 1", 4);
        HospitalityTableJpa jpa = new HospitalityTableJpa(1L, 10L, 5L, "Table 1", 4, TableStatus.AVAILABLE, null, 0L);

        when(springDataRepo.findByOutletId(5L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(table);

        List<HospitalityTable> result = adapter.findByOutletId(5L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should find tables by outlet id and status")
    void shouldFindByOutletIdAndStatus() {
        HospitalityTable table = HospitalityTable.create(1L, 10L, 5L, "Table 1", 4);
        HospitalityTableJpa jpa = new HospitalityTableJpa(1L, 10L, 5L, "Table 1", 4, TableStatus.AVAILABLE, null, 0L);

        when(springDataRepo.findByOutletIdAndStatus(5L, TableStatus.AVAILABLE)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(table);

        List<HospitalityTable> result = adapter.findByOutletIdAndStatus(5L, TableStatus.AVAILABLE);

        assertEquals(1, result.size());
    }
}
