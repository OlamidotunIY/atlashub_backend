package com.atlashub.logistics.shipping.presentation.rest;

import com.atlashub.logistics.shipping.application.commands.AdvanceShipmentMilestone.AdvanceShipmentMilestoneHandler;
import com.atlashub.logistics.shipping.application.commands.AssignRider.AssignRiderHandler;
import com.atlashub.logistics.shipping.application.commands.CompleteDelivery.CompleteDeliveryHandler;
import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentHandler;
import com.atlashub.logistics.shipping.application.commands.RecordFailedDeliveryAttempt.RecordFailedDeliveryAttemptHandler;
import com.atlashub.logistics.shipping.application.commands.Route3PL.Route3PLHandler;
import com.atlashub.logistics.shipping.application.commands.UpdateShipmentLocation.UpdateShipmentLocationHandler;
import com.atlashub.logistics.shipping.application.queries.CalculateShippingQuotes.CalculateShippingQuotesHandler;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode.DeliveryCodeResult;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode.GetShipmentDeliveryCodeHandler;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode.GetShipmentDeliveryCodeQuery;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDetails.GetShipmentDetailsHandler;
import com.atlashub.logistics.shipping.application.queries.ListShipments.ListShipmentsHandler;
import com.atlashub.logistics.shipping.application.queries.TrackShipment.ShipmentTrackingResult;
import com.atlashub.logistics.shipping.application.queries.TrackShipment.TrackShipmentHandler;
import com.atlashub.logistics.shipping.application.queries.TrackShipment.TrackShipmentQuery;
import com.atlashub.shared.application.dto.ApiResponse;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentControllerTest {

    @Mock
    private CreateShipmentHandler createShipmentHandler;
    @Mock
    private CalculateShippingQuotesHandler quotesHandler;
    @Mock
    private GetShipmentDeliveryCodeHandler deliveryCodeHandler;
    @Mock
    private TrackShipmentHandler trackShipmentHandler;
    @Mock
    private GetShipmentDetailsHandler detailsHandler;
    @Mock
    private ListShipmentsHandler listShipmentsHandler;
    @Mock
    private AssignRiderHandler assignRiderHandler;
    @Mock
    private Route3PLHandler route3PLHandler;
    @Mock
    private AdvanceShipmentMilestoneHandler advanceMilestoneHandler;
    @Mock
    private UpdateShipmentLocationHandler updateLocationHandler;
    @Mock
    private CompleteDeliveryHandler completeDeliveryHandler;
    @Mock
    private RecordFailedDeliveryAttemptHandler failedAttemptHandler;

    private ShipmentController controller;

    @BeforeEach
    void setUp() {
        controller = new ShipmentController(
                createShipmentHandler,
                quotesHandler,
                deliveryCodeHandler,
                trackShipmentHandler,
                detailsHandler,
                listShipmentsHandler,
                assignRiderHandler,
                route3PLHandler,
                advanceMilestoneHandler,
                updateLocationHandler,
                completeDeliveryHandler,
                failedAttemptHandler
        );
    }

    @Test
    @DisplayName("Should retrieve delivery code wrapped in ApiResponse")
    void shouldRetrieveDeliveryCode() {
        DeliveryCodeResult codeResult = new DeliveryCodeResult(101L, "ATL-101", "839201", "CREATED");
        when(deliveryCodeHandler.execute(any(GetShipmentDeliveryCodeQuery.class))).thenReturn(codeResult);
        com.atlashub.shared.application.security.AuthenticatedPrincipal principal =
                new com.atlashub.shared.application.security.AuthenticatedPrincipal(
                        10L, 100L, "LIVE", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1)
                );
        ResponseEntity<ApiResponse<DeliveryCodeResult>> response = controller.getDeliveryCode(principal, 101L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("839201", response.getBody().data().deliveryCode());
    }

    @Test
    @DisplayName("Should track shipment publicly")
    void shouldTrackShipment() {
        ShipmentTrackingResult trackingResult = new ShipmentTrackingResult(
                "ATL-101", "IN_TRANSIT", "IN_TRANSIT", "INTRA_STATE",
                "Ikeja", null, null, ZonedDateTime.now(), ZonedDateTime.now().plusHours(24), List.of()
        );
        when(trackShipmentHandler.execute(any(TrackShipmentQuery.class))).thenReturn(trackingResult);

        ResponseEntity<ApiResponse<ShipmentTrackingResult>> response = controller.trackShipment("ATL-101");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("ATL-101", response.getBody().data().trackingNumber());
    }
}
