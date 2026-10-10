package com.atlashub.commerce.inventory.presentation.rest;

import com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn.ApproveCustomerReturnCommand;
import com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn.ApproveCustomerReturnHandler;
import com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn.CreateCustomerReturnCommand;
import com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn.CreateCustomerReturnHandler;
import com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn.CreateCustomerReturnResult;
import com.atlashub.commerce.inventory.domain.valueobject.RefundMethod;
import com.atlashub.commerce.inventory.presentation.dto.CreateCustomerReturnRequest;
import com.atlashub.commerce.inventory.presentation.dto.CreateCustomerReturnResponse;
import com.atlashub.commerce.inventory.presentation.dto.ReturnItemRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerReturnControllerTest {

    @Mock
    private CreateCustomerReturnHandler createCustomerReturnHandler;

    @Mock
    private ApproveCustomerReturnHandler approveCustomerReturnHandler;

    @InjectMocks
    private CustomerReturnController controller;

    private AuthenticatedPrincipal createPrincipal() {
        return new AuthenticatedPrincipal(
                1L,
                10L,
                "LIVE",
                "sess-123",
                "tok-123",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Should create customer return and return 201 with return id")
    void createReturn_shouldReturn201AndReturnId() {
        AuthenticatedPrincipal principal = createPrincipal();
        CreateCustomerReturnRequest request = new CreateCustomerReturnRequest(
                100L,
                20L,
                300L,
                List.of(new ReturnItemRequest(500L, 2)),
                new BigDecimal("15000.00"),
                CurrencyCode.NGN,
                "Damaged in transit",
                RefundMethod.WALLET
        );
        when(createCustomerReturnHandler.execute(any(CreateCustomerReturnCommand.class)))
                .thenReturn(new CreateCustomerReturnResult(99L));

        ResponseEntity<ApiResponse<CreateCustomerReturnResponse>> response =
                controller.createReturn(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(99L, response.getBody().data().returnId());

        ArgumentCaptor<CreateCustomerReturnCommand> captor = ArgumentCaptor.forClass(CreateCustomerReturnCommand.class);
        verify(createCustomerReturnHandler).execute(captor.capture());
        assertEquals(100L, captor.getValue().salesOrderId());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(20L, captor.getValue().outletId());
        assertEquals(300L, captor.getValue().customerId());
        assertEquals(0, captor.getValue().refundAmount().amount().compareTo(new BigDecimal("15000.00")));
        assertEquals(CurrencyCode.NGN, captor.getValue().refundAmount().currency());
        assertEquals("Damaged in transit", captor.getValue().reason());
        assertEquals(RefundMethod.WALLET, captor.getValue().refundMethod());
        assertEquals(1, captor.getValue().items().size());
    }

    @Test
    @DisplayName("Should approve customer return and return 200")
    void approveReturn_shouldReturn200AndInvokeHandler() {
        ResponseEntity<ApiResponse<Void>> response = controller.approveReturn(99L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<ApproveCustomerReturnCommand> captor = ArgumentCaptor.forClass(ApproveCustomerReturnCommand.class);
        verify(approveCustomerReturnHandler).execute(captor.capture());
        assertEquals(99L, captor.getValue().returnId());
    }
}
