package com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetShipmentDeliveryCodeHandlerTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    private GetShipmentDeliveryCodeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetShipmentDeliveryCodeHandler(shipmentRepository);
    }

    @Test
    @DisplayName("Should return 6-digit delivery code for valid shipment")
    void shouldReturnDeliveryCode() {
        ZonedDateTime now = ZonedDateTime.now();
        Shipment shipment = Shipment.create(
                100L,
                10L,
                1L,
                "ATL-100",
                ShipmentType.CUSTOMER_ORDER,
                DeliveryTier.STANDARD,
                new Address("Str", "City", "Lagos", "Nigeria", "100001"),
                new Address("Str", "City", "Lagos", "Nigeria", "100001"),
                10L,
                55L,
                BigDecimal.ONE,
                new Dimensions(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                now.plusHours(12),
                now.plusHours(36),
                now.plusHours(6),
                "corr-code"
        );

        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));

        DeliveryCodeResult result = handler.execute(new GetShipmentDeliveryCodeQuery(100L, 10L));

        assertNotNull(result);
        assertEquals(100L, result.shipmentId());
        assertEquals("ATL-100", result.trackingNumber());
        assertEquals(shipment.getDeliveryCode(), result.deliveryCode());
        assertEquals("CREATED", result.status());
    }

    @Test
    @DisplayName("Should throw ShipmentNotFoundException when shipment does not exist")
    void shouldThrowWhenNotFound() {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ShipmentNotFoundException.class, () ->
                handler.execute(new GetShipmentDeliveryCodeQuery(999L, 10L))
        );
    }
}
