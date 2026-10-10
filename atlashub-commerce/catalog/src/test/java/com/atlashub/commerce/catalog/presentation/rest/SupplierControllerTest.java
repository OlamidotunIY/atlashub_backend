package com.atlashub.commerce.catalog.presentation.rest;

import com.atlashub.commerce.catalog.application.commands.CreateSupplier.CreateSupplierCommand;
import com.atlashub.commerce.catalog.application.commands.CreateSupplier.CreateSupplierHandler;
import com.atlashub.commerce.catalog.application.commands.CreateSupplier.CreateSupplierResult;
import com.atlashub.commerce.catalog.application.queries.ListSuppliers.ListSuppliersHandler;
import com.atlashub.commerce.catalog.application.queries.ListSuppliers.ListSuppliersQuery;
import com.atlashub.commerce.catalog.application.queries.ListSuppliers.SupplierResult;
import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;
import com.atlashub.commerce.catalog.presentation.dto.CreateSupplierRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreateSupplierResponse;
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
class SupplierControllerTest {

    @Mock
    private CreateSupplierHandler createSupplierHandler;

    @Mock
    private ListSuppliersHandler listSuppliersHandler;

    @InjectMocks
    private SupplierController controller;

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
    @DisplayName("Should create supplier and return 201 Created")
    void shouldCreateSupplier() {
        CreateSupplierRequest request = new CreateSupplierRequest(
                "Acme Corp",
                "acme@example.com",
                "+2348011223344",
                "Industrial Area"
        );

        when(createSupplierHandler.execute(any(CreateSupplierCommand.class)))
                .thenReturn(new CreateSupplierResult(77L));

        ResponseEntity<ApiResponse<CreateSupplierResponse>> response = controller.createSupplier(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(77L, response.getBody().data().supplierId());

        ArgumentCaptor<CreateSupplierCommand> captor = ArgumentCaptor.forClass(CreateSupplierCommand.class);
        verify(createSupplierHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals("Acme Corp", captor.getValue().name());
    }

    @Test
    @DisplayName("Should list suppliers")
    void shouldListSuppliers() {
        SupplierResult supplierResult = new SupplierResult(
                77L, 10L, "Acme Corp", "acme@example.com", "+2348011223344", "Address", SupplierStatus.ACTIVE, ZonedDateTime.now()
        );
        when(listSuppliersHandler.execute(any(ListSuppliersQuery.class))).thenReturn(List.of(supplierResult));

        ResponseEntity<ApiResponse<List<SupplierResult>>> response = controller.listSuppliers(principal, SupplierStatus.ACTIVE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(1, response.getBody().data().size());
        assertEquals(supplierResult, response.getBody().data().get(0));
    }
}
