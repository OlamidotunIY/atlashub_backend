package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ShipmentJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.mappers.ShipmentMapper;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataShipmentRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentRepositoryAdapterTest {

    @Mock
    private SpringDataShipmentRepository springDataRepo;

    @Mock
    private ShipmentMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private ShipmentRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ShipmentRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should find shipment by tracking number")
    void shouldFindByTrackingNumber() {
        ShipmentJpa jpa = org.mockito.Mockito.mock(ShipmentJpa.class);
        ZonedDateTime now = ZonedDateTime.now();
        Shipment domain = Shipment.create(
                1L, 100L, 10L, "TRK-999", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                new Address("Street", "City", "Lagos", "Nigeria", "100001"),
                new Address("Street", "City", "Lagos", "Nigeria", "100001"),
                100L, 10L,
                BigDecimal.ONE, new Dimensions(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                now.plusHours(12), now.plusHours(36), now.plusHours(6), "corr-adapter"
        );

        when(springDataRepo.findByTrackingNumber("TRK-999")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Shipment> result = adapter.findByTrackingNumber("TRK-999");

        assertTrue(result.isPresent());
        assertEquals("TRK-999", result.get().getTrackingNumber());
    }
}
