package com.atlashub.commerce.catalog.presentation.rest;

import com.atlashub.commerce.catalog.application.commands.ApproveVendor.ApproveVendorCommand;
import com.atlashub.commerce.catalog.application.commands.ApproveVendor.ApproveVendorHandler;
import com.atlashub.commerce.catalog.application.commands.CreateVendor.CreateVendorCommand;
import com.atlashub.commerce.catalog.application.commands.CreateVendor.CreateVendorHandler;
import com.atlashub.commerce.catalog.application.commands.CreateVendor.CreateVendorResult;
import com.atlashub.commerce.catalog.application.commands.SuspendVendor.SuspendVendorCommand;
import com.atlashub.commerce.catalog.application.commands.SuspendVendor.SuspendVendorHandler;
import com.atlashub.commerce.catalog.application.queries.ListVendors.ListVendorsHandler;
import com.atlashub.commerce.catalog.application.queries.ListVendors.ListVendorsQuery;
import com.atlashub.commerce.catalog.application.queries.ListVendors.VendorResult;
import com.atlashub.commerce.catalog.domain.valueobject.DisbursementSchedule;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
import com.atlashub.commerce.catalog.presentation.dto.CreateVendorRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreateVendorResponse;
import com.atlashub.commerce.catalog.presentation.dto.SuspendVendorRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorControllerTest {

    @Mock
    private CreateVendorHandler createVendorHandler;

    @Mock
    private ApproveVendorHandler approveVendorHandler;

    @Mock
    private SuspendVendorHandler suspendVendorHandler;

    @Mock
    private ListVendorsHandler listVendorsHandler;

    @InjectMocks
    private VendorController controller;

    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedPrincipal(
                1L,
                10L,
                "LIVE",
                "sess-1",
                "tok-1",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Should create vendor and return 201 Created")
    void shouldCreateVendor() {
        CreateVendorRequest request = new CreateVendorRequest(
                555L,
                "Vendor Corp",
                "vendor@corp.com",
                "+2348011223344",
                "044",
                "0123456789",
                "Vendor Corp Ltd",
                new BigDecimal("0.05"),
                DisbursementSchedule.WEEKLY
        );

        when(createVendorHandler.execute(any(CreateVendorCommand.class)))
                .thenReturn(new CreateVendorResult(66L));

        ResponseEntity<ApiResponse<CreateVendorResponse>> response = controller.createVendor(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(66L, response.getBody().data().vendorId());

        ArgumentCaptor<CreateVendorCommand> captor = ArgumentCaptor.forClass(CreateVendorCommand.class);
        verify(createVendorHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(555L, captor.getValue().userId());
        assertEquals("Vendor Corp", captor.getValue().businessName());
    }

    @Test
    @DisplayName("Should approve vendor")
    void shouldApproveVendor() {
        ResponseEntity<ApiResponse<Void>> response = controller.approveVendor(66L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<ApproveVendorCommand> captor = ArgumentCaptor.forClass(ApproveVendorCommand.class);
        verify(approveVendorHandler).execute(captor.capture());
        assertEquals(66L, captor.getValue().vendorId());
    }

    @Test
    @DisplayName("Should suspend vendor")
    void shouldSuspendVendor() {
        SuspendVendorRequest request = new SuspendVendorRequest("Policy violation");

        ResponseEntity<ApiResponse<Void>> response = controller.suspendVendor(66L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<SuspendVendorCommand> captor = ArgumentCaptor.forClass(SuspendVendorCommand.class);
        verify(suspendVendorHandler).execute(captor.capture());
        assertEquals(66L, captor.getValue().vendorId());
        assertEquals("Policy violation", captor.getValue().reason());
    }

    @Test
    @DisplayName("Should list vendors")
    void shouldListVendors() {
        VendorResult vendorResult = new VendorResult(
                66L, 10L, 555L, "Vendor Corp", "vendor@corp.com", "+2348011223344",
                "044", "0123456789", "Vendor Corp Ltd", new BigDecimal("0.05"),
                DisbursementSchedule.WEEKLY, VendorStatus.ACTIVE, ZonedDateTime.now()
        );
        when(listVendorsHandler.execute(any(ListVendorsQuery.class))).thenReturn(List.of(vendorResult));

        ResponseEntity<ApiResponse<List<VendorResult>>> response = controller.listVendors(principal, VendorStatus.ACTIVE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(1, response.getBody().data().size());
        assertEquals(vendorResult, response.getBody().data().get(0));
    }
}
