package com.atlashub.logistics.shipping.application.queries.TrackShipment;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.entities.ShipmentMilestoneLog;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentMilestoneLogRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackShipmentHandlerTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentMilestoneLogRepository milestoneLogRepository;

    private TrackShipmentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TrackShipmentHandler(shipmentRepository, milestoneLogRepository);
    }

    @Test
    @DisplayName("Should return tracking details with milestone history")
    void shouldTrackShipment() {
        ZonedDateTime now = ZonedDateTime.now();
        Shipment shipment = Shipment.create(
                10L,
                100L,
                10L,
                "ATL-TRK-777",
                ShipmentType.CUSTOMER_ORDER,
                DeliveryTier.STANDARD,
                new Address("Street 1", "City", "Lagos", "Nigeria", "100001"),
                new Address("Street 2", "City", "Abuja", "Nigeria", "900001"),
                100L,
                50L,
                BigDecimal.ONE,
                new Dimensions(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN),
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("4000"), CurrencyCode.NGN),
                now.plusHours(24),
                now.plusHours(72),
                now.plusHours(12),
                "corr-track"
        );

        ShipmentMilestoneLog log = ShipmentMilestoneLog.create(
                1L,
                10L,
                "ATL-TRK-777",
                RouteType.INTER_STATE,
                TrackingMilestone.CREATED,
                ShipmentStatus.CREATED,
                "Lagos Hub",
                "Lagos",
                "Shipment created",
                null,
                null
        );

        when(shipmentRepository.findByTrackingNumber("ATL-TRK-777")).thenReturn(Optional.of(shipment));
        when(milestoneLogRepository.findByTrackingNumberOrderByOccurredAtAsc("ATL-TRK-777")).thenReturn(List.of(log));

        ShipmentTrackingResult result = handler.execute(new TrackShipmentQuery("ATL-TRK-777"));

        assertNotNull(result);
        assertEquals("ATL-TRK-777", result.trackingNumber());
        assertEquals("CREATED", result.status());
        assertEquals("CREATED", result.currentMilestone());
        assertEquals("INTER_STATE", result.routeType());
        assertEquals(1, result.milestoneHistory().size());
        assertEquals("CREATED", result.milestoneHistory().get(0).milestone());
    }
}
