package com.atlashub.logistics.shipping.application.commands.CompleteDelivery;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.entities.ShipmentMilestoneLog;
import com.atlashub.logistics.shipping.domain.exceptions.InvalidDeliveryCodeException;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.DispatchRiderRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentMilestoneLogRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.RiderStatus;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteDeliveryHandlerTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private DispatchRiderRepository riderRepository;

    @Mock
    private ShipmentMilestoneLogRepository milestoneLogRepository;

    private CompleteDeliveryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CompleteDeliveryHandler(shipmentRepository, riderRepository, milestoneLogRepository);
    }

    @Test
    @DisplayName("Should complete delivery when code and proof are valid, and release rider")
    void shouldCompleteDeliveryAndReleaseRider() {
        ZonedDateTime now = ZonedDateTime.now();
        Shipment shipment = Shipment.create(
                101L,
                100L,
                10L,
                "ATL-101",
                ShipmentType.CUSTOMER_ORDER,
                DeliveryTier.STANDARD,
                new Address("Street A", "Ikeja", "Lagos", "Nigeria", "100001"),
                new Address("Street B", "Yaba", "Lagos", "Nigeria", "100001"),
                100L,
                55L,
                new BigDecimal("1.5"),
                new Dimensions(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                now.plusHours(12),
                now.plusHours(36),
                now.plusHours(6),
                "corr-101"
        );
        shipment.assignInternalRider(201L, 301L, "corr-assign");
        shipment.advanceMilestone(TrackingMilestone.PICKED_UP_FROM_STORE, "Ikeja Hub", "Lagos", "Picked up", null, null, now, "corr-adv");

        DispatchRider rider = DispatchRider.create(
                201L,
                100L,
                55L,
                "LAG-IKJ",
                301L,
                "LIC-123"
        );
        rider.assignDelivery(101L, 301L);

        when(shipmentRepository.findById(101L)).thenReturn(Optional.of(shipment));
        when(riderRepository.findById(201L)).thenReturn(Optional.of(rider));
        when(milestoneLogRepository.nextIdentity()).thenReturn(9001L);

        CompleteDeliveryCommand command = new CompleteDeliveryCommand(
                101L,
                shipment.getDeliveryCode(),
                "https://cdn.atlashub.com/pod.png",
                "corr-123"
        );

        handler.execute(command);

        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
        assertEquals(RiderStatus.AVAILABLE, rider.getStatus());

        verify(shipmentRepository).save(shipment);
        verify(riderRepository).save(rider);
        verify(milestoneLogRepository).save(any(ShipmentMilestoneLog.class));
    }

    @Test
    @DisplayName("Should throw InvalidDeliveryCodeException if handover code is incorrect")
    void shouldThrowWhenCodeIsInvalid() {
        ZonedDateTime now = ZonedDateTime.now();
        Shipment shipment = Shipment.create(
                102L,
                100L,
                10L,
                "ATL-102",
                ShipmentType.CUSTOMER_ORDER,
                DeliveryTier.STANDARD,
                new Address("Street A", "Ikeja", "Lagos", "Nigeria", "100001"),
                new Address("Street B", "Yaba", "Lagos", "Nigeria", "100001"),
                100L,
                55L,
                new BigDecimal("1.5"),
                new Dimensions(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                now.plusHours(12),
                now.plusHours(36),
                now.plusHours(6),
                "corr-102"
        );
        shipment.assignInternalRider(201L, 301L, "corr-assign");
        shipment.advanceMilestone(TrackingMilestone.PICKED_UP_FROM_STORE, "Ikeja Hub", "Lagos", "Picked up", null, null, now, "corr-adv");

        when(shipmentRepository.findById(102L)).thenReturn(Optional.of(shipment));

        CompleteDeliveryCommand command = new CompleteDeliveryCommand(
                102L,
                "000000",
                "https://cdn.atlashub.com/pod.png",
                "corr-456"
        );

        assertThrows(InvalidDeliveryCodeException.class, () -> handler.execute(command));
    }
}
