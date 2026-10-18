package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ShipmentJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataShipmentRepository;
import com.atlashub.shared.application.port.LogisticsShippingQueryPort;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogisticsShippingQueryPortAdapterTest {

    @Mock
    private SpringDataShipmentRepository springDataRepo;

    private LogisticsShippingQueryPortAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LogisticsShippingQueryPortAdapter(springDataRepo);
    }

    @Test
    @DisplayName("Should find shipment snapshot by ID")
    void shouldFindSnapshotById() {
        ZonedDateTime now = ZonedDateTime.now();
        ShipmentJpa jpa = Mockito.mock(ShipmentJpa.class);
        when(jpa.getId()).thenReturn(101L);
        when(jpa.getOrganizationId()).thenReturn(10L);
        when(jpa.getOrderId()).thenReturn(55L);
        when(jpa.getTrackingNumber()).thenReturn("ATL-SNAPSHOT-1");
        when(jpa.getStatus()).thenReturn(ShipmentStatus.IN_TRANSIT);
        when(jpa.getServiceTier()).thenReturn(DeliveryTier.STANDARD);
        when(jpa.getRouteType()).thenReturn(RouteType.INTRA_STATE);
        when(jpa.getCurrentMilestone()).thenReturn(TrackingMilestone.IN_TRANSIT);
        when(jpa.getDeliveryCode()).thenReturn("123456");
        when(jpa.getDeliveryWindowStart()).thenReturn(now);
        when(jpa.getDeliveryWindowEnd()).thenReturn(now.plusHours(24));

        when(springDataRepo.findById(101L)).thenReturn(Optional.of(jpa));

        Optional<LogisticsShippingQueryPort.ShippingSnapshot> snapshot = adapter.findShipmentById(101L);

        assertTrue(snapshot.isPresent());
        assertEquals(101L, snapshot.get().shipmentId());
        assertEquals("ATL-SNAPSHOT-1", snapshot.get().trackingNumber());
        assertEquals("IN_TRANSIT", snapshot.get().status());
        assertEquals("123456", snapshot.get().deliveryCode());
    }
}
