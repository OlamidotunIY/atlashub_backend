package com.atlashub.logistics.warehousing.domain.entities;

import com.atlashub.logistics.warehousing.domain.events.WarehouseCreatedEvent;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.FacilityType;
import com.atlashub.logistics.warehousing.domain.valueobject.WarehouseStatus;
import com.atlashub.logistics.warehousing.domain.exceptions.WarehousingInvariantException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarehouseTest {

    private final Address testAddress = new Address("12 Broad St", "Marina", "Lagos", "Nigeria", "100001");

    @Test
    @DisplayName("Should create active warehouse and register WarehouseCreatedEvent")
    void shouldCreateWarehouse() {
        Warehouse warehouse = Warehouse.create(
                1L,
                100L,
                "Marina Distribution Hub",
                "DC-MARINA-01",
                FacilityType.ATLASHUB_DISTRIBUTION_CENTER,
                testAddress,
                55L,
                "corr-wh-1"
        );

        assertNotNull(warehouse);
        assertEquals(1L, warehouse.getId());
        assertEquals("Marina Distribution Hub", warehouse.getName());
        assertEquals("DC-MARINA-01", warehouse.getCode());
        assertEquals(WarehouseStatus.ACTIVE, warehouse.getStatus());
        assertEquals(FacilityType.ATLASHUB_DISTRIBUTION_CENTER, warehouse.getFacilityType());

        assertFalse(warehouse.peekDomainEvents().isEmpty());
        assertTrue(warehouse.peekDomainEvents().get(0) instanceof WarehouseCreatedEvent);
    }

    @Test
    @DisplayName("Should reject warehouse creation when name or code is blank")
    void shouldRejectInvalidWarehouse() {
        assertThrows(WarehousingInvariantException.class, () ->
                Warehouse.create(1L, 100L, "", "CODE", FacilityType.MERCHANT_WAREHOUSE, testAddress, 55L, "corr")
        );

        assertThrows(WarehousingInvariantException.class, () ->
                Warehouse.create(1L, 100L, "Warehouse", "", FacilityType.MERCHANT_WAREHOUSE, testAddress, 55L, "corr")
        );
    }

    @Test
    @DisplayName("Should deactivate and reactivate warehouse successfully")
    void shouldDeactivateAndReactivate() {
        Warehouse warehouse = Warehouse.create(
                2L,
                100L,
                "Ikeja Merchant Store",
                "WH-IKJ-01",
                FacilityType.MERCHANT_WAREHOUSE,
                testAddress,
                10L,
                "corr-wh-2"
        );

        warehouse.deactivate();
        assertEquals(WarehouseStatus.INACTIVE, warehouse.getStatus());

        warehouse.reactivate();
        assertEquals(WarehouseStatus.ACTIVE, warehouse.getStatus());
    }
}
