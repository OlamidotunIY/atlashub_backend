package com.atlashub.logistics.shipping.application.commands.CreateShipment;

import com.atlashub.logistics.shipping.domain.entities.CoverageZone;
import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.repositories.CoverageZoneRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentMilestoneLogRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateShipmentHandlerTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CoverageZoneRepository coverageZoneRepository;

    @Mock
    private ShipmentMilestoneLogRepository milestoneLogRepository;

    private CreateShipmentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateShipmentHandler(shipmentRepository, coverageZoneRepository, milestoneLogRepository);
    }

    @Test
    @DisplayName("Should execute CreateShipmentCommand and return result with tracking number and delivery code")
    void shouldExecuteCreateShipment() {
        when(shipmentRepository.nextIdentity()).thenReturn(1001L);
        when(milestoneLogRepository.nextIdentity()).thenReturn(5001L);
        when(coverageZoneRepository.findByStateAndCity("Lagos", "Ikeja")).thenReturn(Optional.of(
                CoverageZone.create(
                        1L, "Lagos", "Ikeja", "LAG-IKJ", true, true, "GIG_LOGISTICS", true,
                        Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                        Money.of(new BigDecimal("300"), CurrencyCode.NGN),
                        new BigDecimal("2.50"),
                        12, 36, 48, 96, 2, 6
                )
        ));

        Address origin = new Address("1 Broad St", "Lagos Island", "Lagos", "Nigeria", "100001");
        Address destination = new Address("15 Allen Ave", "Ikeja", "Lagos", "Nigeria", "100001");
        Dimensions dimensions = new Dimensions(new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"));

        CreateShipmentCommand command = new CreateShipmentCommand(
                100L,
                10L,
                ShipmentType.CUSTOMER_ORDER,
                DeliveryTier.STANDARD,
                origin,
                destination,
                100L,
                50L,
                new BigDecimal("2.0"),
                dimensions,
                null,
                null
        );

        CreateShipmentResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(1001L, result.shipmentId());
        assertNotNull(result.trackingNumber());
        assertNotNull(result.deliveryCode());
        assertEquals(6, result.deliveryCode().length());
        assertEquals("INTRA_STATE", result.routeType());
        assertEquals("STANDARD", result.serviceTier());

        verify(shipmentRepository).save(any(Shipment.class));
    }
}
