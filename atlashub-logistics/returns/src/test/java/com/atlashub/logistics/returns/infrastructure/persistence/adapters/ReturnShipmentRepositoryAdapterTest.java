package com.atlashub.logistics.returns.infrastructure.persistence.adapters;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.logistics.returns.domain.valueobject.ReturnFaultType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnReasonType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnStatus;
import com.atlashub.logistics.returns.infrastructure.persistence.entities.ReturnShipmentJpa;
import com.atlashub.logistics.returns.infrastructure.persistence.mappers.ReturnShipmentMapper;
import com.atlashub.logistics.returns.infrastructure.persistence.repositories.SpringDataReturnShipmentRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnShipmentRepositoryAdapterTest {

    @Mock
    private SpringDataReturnShipmentRepository springDataRepo;

    @Mock
    private ReturnShipmentMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private ReturnShipmentRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ReturnShipmentRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should find return shipment by tracking number via SpringData and MapStruct")
    void shouldFindByReturnTrackingNumber() {
        ReturnShipmentJpa jpa = mock(ReturnShipmentJpa.class);
        ReturnShipment domain = mock(ReturnShipment.class);

        when(springDataRepo.findByReturnTrackingNumber("RET-001")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<ReturnShipment> result = adapter.findByReturnTrackingNumber("RET-001");

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find return shipments by organization ID")
    void shouldFindByOrganizationId() {
        ReturnShipmentJpa jpa = mock(ReturnShipmentJpa.class);
        ReturnShipment domain = mock(ReturnShipment.class);

        when(springDataRepo.findByOrganizationId(100L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<ReturnShipment> results = adapter.findByOrganizationId(100L);

        assertEquals(1, results.size());
        assertEquals(domain, results.get(0));
    }

    @Test
    @DisplayName("Should find return shipment by original shipment ID")
    void shouldFindByOriginalShipmentId() {
        ReturnShipmentJpa jpa = mock(ReturnShipmentJpa.class);
        ReturnShipment domain = mock(ReturnShipment.class);

        when(springDataRepo.findByOriginalShipmentId(701L)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<ReturnShipment> result = adapter.findByOriginalShipmentId(701L);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should return configured sequence name")
    void shouldReturnConfiguredSequenceName() {
        assertEquals("returns_shipment_seq", adapter.getSequenceName());
    }
}
