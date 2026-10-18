package com.atlashub.logistics.warehousing.infrastructure.persistence.adapters;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.FacilityType;
import com.atlashub.logistics.warehousing.domain.valueobject.WarehouseStatus;
import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.WarehouseJpa;
import com.atlashub.logistics.warehousing.infrastructure.persistence.mappers.WarehouseMapper;
import com.atlashub.logistics.warehousing.infrastructure.persistence.repositories.SpringDataWarehouseRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseRepositoryAdapterTest {

    @Mock
    private SpringDataWarehouseRepository springDataRepo;

    @Mock
    private WarehouseMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private WarehouseRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WarehouseRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should find warehouse by code via SpringData and MapStruct")
    void shouldFindByCode() {
        WarehouseJpa jpa = new WarehouseJpa(
                1L, 100L, "Main Hub", "DC-LOS-01", FacilityType.ATLASHUB_DISTRIBUTION_CENTER,
                "Marina", "Lagos", "Lagos", "Nigeria", "100001", 10L, WarehouseStatus.ACTIVE,
                ZonedDateTime.now(), 0L
        );
        Warehouse domain = new Warehouse(
                1L, 100L, "Main Hub", "DC-LOS-01", FacilityType.ATLASHUB_DISTRIBUTION_CENTER,
                new Address("Marina", "Lagos", "Lagos", "Nigeria", "100001"), 10L, WarehouseStatus.ACTIVE,
                ZonedDateTime.now()
        );

        when(springDataRepo.findByCode("DC-LOS-01")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Warehouse> result = adapter.findByCode("DC-LOS-01");

        assertTrue(result.isPresent());
        assertEquals("DC-LOS-01", result.get().getCode());
        assertEquals("Main Hub", result.get().getName());
    }
}
