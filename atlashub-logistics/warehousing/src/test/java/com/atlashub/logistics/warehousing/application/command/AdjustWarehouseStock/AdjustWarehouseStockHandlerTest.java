package com.atlashub.logistics.warehousing.application.command.AdjustWarehouseStock;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.logistics.warehousing.domain.entities.WarehouseInventory;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseInventoryRepository;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseRepository;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.FacilityType;
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
class AdjustWarehouseStockHandlerTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseInventoryRepository inventoryRepository;

    private AdjustWarehouseStockHandler handler;

    private final Address address = new Address("Main St", "Ikeja", "Lagos", "Nigeria", "100001");

    @BeforeEach
    void setUp() {
        handler = new AdjustWarehouseStockHandler(warehouseRepository, inventoryRepository);
    }

    @Test
    @DisplayName("Should create new inventory record when not already existing")
    void shouldCreateNewInventoryOnAdjust() {
        Warehouse warehouse = Warehouse.create(
                1L, 100L, "Main WH", "WH-01", FacilityType.MERCHANT_WAREHOUSE, address, 10L, "corr"
        );
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByWarehouseIdAndProductId(1L, 501L)).thenReturn(Optional.empty());
        when(inventoryRepository.nextIdentity()).thenReturn(201L);

        AdjustWarehouseStockCommand command = new AdjustWarehouseStockCommand(
                100L, 1L, 501L, 50, "corr-adj-1"
        );

        AdjustWarehouseStockResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(201L, result.inventoryId());
        assertEquals(50, result.quantity());
        assertEquals(0, result.reservedQuantity());
        assertEquals(50, result.availableQuantity());

        verify(inventoryRepository).save(any(WarehouseInventory.class));
    }
}
