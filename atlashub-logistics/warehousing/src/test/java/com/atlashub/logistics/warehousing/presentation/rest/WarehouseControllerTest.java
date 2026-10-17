package com.atlashub.logistics.warehousing.presentation.rest;

import com.atlashub.logistics.warehousing.application.command.AdjustWarehouseStock.AdjustWarehouseStockHandler;
import com.atlashub.logistics.warehousing.application.command.ApproveStockTransfer.ApproveStockTransferHandler;
import com.atlashub.logistics.warehousing.application.command.DispatchStockTransfer.DispatchStockTransferHandler;
import com.atlashub.logistics.warehousing.application.command.ReceiveStockTransfer.ReceiveStockTransferHandler;
import com.atlashub.logistics.warehousing.application.command.RegisterWarehouse.RegisterWarehouseCommand;
import com.atlashub.logistics.warehousing.application.command.RegisterWarehouse.RegisterWarehouseHandler;
import com.atlashub.logistics.warehousing.application.command.RegisterWarehouse.RegisterWarehouseResult;
import com.atlashub.logistics.warehousing.application.command.RequestStockTransfer.RequestStockTransferHandler;
import com.atlashub.logistics.warehousing.application.query.GetStockTransfer.GetStockTransferHandler;
import com.atlashub.logistics.warehousing.application.query.GetWarehouse.GetWarehouseHandler;
import com.atlashub.logistics.warehousing.application.query.GetWarehouse.GetWarehouseQuery;
import com.atlashub.logistics.warehousing.application.query.GetWarehouse.WarehouseResult;
import com.atlashub.logistics.warehousing.application.query.GetWarehouseInventory.GetWarehouseInventoryHandler;
import com.atlashub.logistics.warehousing.application.query.ListStockTransfers.ListStockTransfersHandler;
import com.atlashub.logistics.warehousing.application.query.ListWarehouses.ListWarehousesHandler;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.FacilityType;
import com.atlashub.logistics.warehousing.presentation.dto.RegisterWarehouseRequest;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.application.dto.ApiResponse;
import java.time.ZonedDateTime;
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
class WarehouseControllerTest {

    @Mock
    private RegisterWarehouseHandler registerWarehouseHandler;
    @Mock
    private AdjustWarehouseStockHandler adjustWarehouseStockHandler;
    @Mock
    private RequestStockTransferHandler requestStockTransferHandler;
    @Mock
    private ApproveStockTransferHandler approveStockTransferHandler;
    @Mock
    private DispatchStockTransferHandler dispatchStockTransferHandler;
    @Mock
    private ReceiveStockTransferHandler receiveStockTransferHandler;
    @Mock
    private GetWarehouseHandler getWarehouseHandler;
    @Mock
    private ListWarehousesHandler listWarehousesHandler;
    @Mock
    private GetWarehouseInventoryHandler getWarehouseInventoryHandler;
    @Mock
    private GetStockTransferHandler getStockTransferHandler;
    @Mock
    private ListStockTransfersHandler listStockTransfersHandler;

    private WarehouseController controller;

    private final Address address = new Address("12 Marina", "Lagos Island", "Lagos", "Nigeria", "100001");
    private final AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            10L, 100L, "LIVE", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1)
    );

    @BeforeEach
    void setUp() {
        controller = new WarehouseController(
                registerWarehouseHandler,
                adjustWarehouseStockHandler,
                requestStockTransferHandler,
                approveStockTransferHandler,
                dispatchStockTransferHandler,
                receiveStockTransferHandler,
                getWarehouseHandler,
                listWarehousesHandler,
                getWarehouseInventoryHandler,
                getStockTransferHandler,
                listStockTransfersHandler
        );
    }

    @Test
    @DisplayName("Should register warehouse and return CREATED ApiResponse")
    void shouldRegisterWarehouse() {
        RegisterWarehouseRequest request = new RegisterWarehouseRequest(
                "Marina Central", "DC-MAR-01", FacilityType.ATLASHUB_DISTRIBUTION_CENTER, address, 10L
        );
        RegisterWarehouseResult result = new RegisterWarehouseResult(
                1L, "Marina Central", "DC-MAR-01", "ATLASHUB_DISTRIBUTION_CENTER", "ACTIVE"
        );
        when(registerWarehouseHandler.execute(any(RegisterWarehouseCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<RegisterWarehouseResult>> response = controller.registerWarehouse(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(1L, response.getBody().data().warehouseId());
    }

    @Test
    @DisplayName("Should get warehouse by query parameter ID")
    void shouldGetWarehouseById() {
        WarehouseResult result = new WarehouseResult(
                1L, 100L, "Marina Central", "DC-MAR-01", "ATLASHUB_DISTRIBUTION_CENTER",
                address, 10L, "ACTIVE", ZonedDateTime.now()
        );
        when(getWarehouseHandler.execute(any(GetWarehouseQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<Object>> response = controller.getWarehouses(principal, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
    }
}
