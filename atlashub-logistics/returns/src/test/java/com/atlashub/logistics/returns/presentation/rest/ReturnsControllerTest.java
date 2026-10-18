package com.atlashub.logistics.returns.presentation.rest;

import com.atlashub.logistics.returns.application.command.CreateReturnShipment.CreateReturnShipmentCommand;
import com.atlashub.logistics.returns.application.command.CreateReturnShipment.CreateReturnShipmentHandler;
import com.atlashub.logistics.returns.application.command.CreateReturnShipment.CreateReturnShipmentResult;
import com.atlashub.logistics.returns.application.command.MarkReturnInTransit.MarkReturnInTransitCommand;
import com.atlashub.logistics.returns.application.command.MarkReturnInTransit.MarkReturnInTransitHandler;
import com.atlashub.logistics.returns.application.command.MarkReturnInTransit.MarkReturnInTransitResult;
import com.atlashub.logistics.returns.application.command.ReceiveReturnShipment.ReceiveReturnShipmentCommand;
import com.atlashub.logistics.returns.application.command.ReceiveReturnShipment.ReceiveReturnShipmentHandler;
import com.atlashub.logistics.returns.application.command.ReceiveReturnShipment.ReceiveReturnShipmentResult;
import com.atlashub.logistics.returns.application.command.RejectReturnShipment.RejectReturnShipmentCommand;
import com.atlashub.logistics.returns.application.command.RejectReturnShipment.RejectReturnShipmentHandler;
import com.atlashub.logistics.returns.application.command.RejectReturnShipment.RejectReturnShipmentResult;
import com.atlashub.logistics.returns.application.query.GetReturnShipment.GetReturnShipmentHandler;
import com.atlashub.logistics.returns.application.query.GetReturnShipment.GetReturnShipmentQuery;
import com.atlashub.logistics.returns.application.query.GetReturnShipment.ReturnShipmentResult;
import com.atlashub.logistics.returns.application.query.ListReturnShipments.ListReturnShipmentsHandler;
import com.atlashub.logistics.returns.application.query.ListReturnShipments.ListReturnShipmentsQuery;
import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.logistics.returns.domain.valueobject.ReturnFaultType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnReasonType;
import com.atlashub.logistics.returns.presentation.dto.CreateReturnShipmentRequest;
import com.atlashub.logistics.returns.presentation.dto.ReceiveReturnShipmentRequest;
import com.atlashub.logistics.returns.presentation.dto.RejectReturnShipmentRequest;
import com.atlashub.logistics.returns.presentation.dto.ReturnShipmentResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
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
class ReturnsControllerTest {

    @Mock
    private CreateReturnShipmentHandler createReturnShipmentHandler;
    @Mock
    private MarkReturnInTransitHandler markReturnInTransitHandler;
    @Mock
    private ReceiveReturnShipmentHandler receiveReturnShipmentHandler;
    @Mock
    private RejectReturnShipmentHandler rejectReturnShipmentHandler;
    @Mock
    private GetReturnShipmentHandler getReturnShipmentHandler;
    @Mock
    private ListReturnShipmentsHandler listReturnShipmentsHandler;

    private ReturnsController controller;

    private final Address address = new Address("45 Customer St", "Ikeja", "Lagos", "Nigeria", "100001");
    private final AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            10L, 100L, "LIVE", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1)
    );

    @BeforeEach
    void setUp() {
        controller = new ReturnsController(
                createReturnShipmentHandler,
                markReturnInTransitHandler,
                receiveReturnShipmentHandler,
                rejectReturnShipmentHandler,
                getReturnShipmentHandler,
                listReturnShipmentsHandler
        );
    }

    @Test
    @DisplayName("Should create return shipment and return CREATED ApiResponse")
    void shouldCreateReturnShipment() {
        Money itemAmount = Money.of(new BigDecimal("10000"), CurrencyCode.NGN);
        Money shippingFee = Money.of(new BigDecimal("2000"), CurrencyCode.NGN);
        Money returnFee = Money.of(new BigDecimal("1500"), CurrencyCode.NGN);
        Money declaredVal = Money.of(new BigDecimal("10000"), CurrencyCode.NGN);

        CreateReturnShipmentRequest request = new CreateReturnShipmentRequest(
                501L, 701L, 999L, address, 10L,
                ReturnReasonType.FAILED_DELIVERY_RTO,
                ReturnFaultType.CUSTOMER_FAULT,
                2,
                itemAmount, shippingFee, returnFee, declaredVal
        );

        CreateReturnShipmentResult result = new CreateReturnShipmentResult(
                1L, "RET-001", "REQUESTED", "CUSTOMER_FAULT",
                Money.of(new BigDecimal("8500"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                false
        );

        when(createReturnShipmentHandler.execute(any(CreateReturnShipmentCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<CreateReturnShipmentResult>> response = controller.createReturn(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("RET-001", response.getBody().data().returnTrackingNumber());
    }

    @Test
    @DisplayName("Should mark return shipment in transit")
    void shouldMarkReturnInTransit() {
        MarkReturnInTransitResult result = new MarkReturnInTransitResult(1L, "IN_TRANSIT");
        when(markReturnInTransitHandler.execute(any(MarkReturnInTransitCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<MarkReturnInTransitResult>> response = controller.markInTransit(principal, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("IN_TRANSIT", response.getBody().data().status());
    }

    @Test
    @DisplayName("Should receive return shipment and confirm refund")
    void shouldReceiveReturnShipment() {
        ReceiveReturnShipmentRequest request = new ReceiveReturnShipmentRequest(
                "Inspected and items verified",
                ZonedDateTime.now()
        );

        ReceiveReturnShipmentResult result = new ReceiveReturnShipmentResult(
                1L, "RECEIVED",
                Money.of(new BigDecimal("8500"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                false,
                request.receivedAt()
        );

        when(receiveReturnShipmentHandler.execute(any(ReceiveReturnShipmentCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<ReceiveReturnShipmentResult>> response = controller.receiveReturn(principal, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("RECEIVED", response.getBody().data().status());
    }

    @Test
    @DisplayName("Should reject return shipment after inspection")
    void shouldRejectReturnShipment() {
        RejectReturnShipmentRequest request = new RejectReturnShipmentRequest("Item tampered with");
        RejectReturnShipmentResult result = new RejectReturnShipmentResult(1L, "REJECTED", "Item tampered with");

        when(rejectReturnShipmentHandler.execute(any(RejectReturnShipmentCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<RejectReturnShipmentResult>> response = controller.rejectReturn(principal, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("REJECTED", response.getBody().data().status());
    }

    @Test
    @DisplayName("Should get return shipment details by ID")
    void shouldGetReturnDetails() {
        ReturnShipmentResult result = new ReturnShipmentResult(
                1L, 100L, 501L, 701L, 999L, address, 10L,
                "RET-001", "FAILED_DELIVERY_RTO", "CUSTOMER_FAULT", 2,
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("8500"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                false, "REQUESTED", null,
                ZonedDateTime.now(), null
        );

        when(getReturnShipmentHandler.execute(any(GetReturnShipmentQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<ReturnShipmentResponse>> response = controller.getReturnDetails(principal, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("RET-001", response.getBody().data().returnTrackingNumber());
    }

    @Test
    @DisplayName("Should list return shipments for organization")
    void shouldListReturns() {
        ReturnShipmentResult result = new ReturnShipmentResult(
                1L, 100L, 501L, 701L, 999L, address, 10L,
                "RET-001", "FAILED_DELIVERY_RTO", "CUSTOMER_FAULT", 2,
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("8500"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                false, "REQUESTED", null,
                ZonedDateTime.now(), null
        );

        when(listReturnShipmentsHandler.execute(any(ListReturnShipmentsQuery.class))).thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<ReturnShipmentResponse>>> response = controller.listReturns(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(1, response.getBody().data().size());
    }
}
