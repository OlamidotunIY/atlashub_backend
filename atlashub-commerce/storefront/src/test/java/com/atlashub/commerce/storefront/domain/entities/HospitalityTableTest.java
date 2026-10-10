package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.exceptions.TableAlreadyOccupiedException;
import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HospitalityTableTest {

    @Test
    @DisplayName("Should create table in AVAILABLE status")
    void create_shouldInitializeTable() {
        HospitalityTable table = HospitalityTable.create(1L, 10L, 20L, "T-12", 4);

        assertEquals(1L, table.getId());
        assertEquals("T-12", table.getTableNumber());
        assertEquals(4, table.getCovers());
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        assertNull(table.getCurrentOrderId());
    }

    @Test
    @DisplayName("Should occupy table and update currentOrderId and covers")
    void occupy_shouldUpdateStatusAndOrder() {
        HospitalityTable table = HospitalityTable.create(1L, 10L, 20L, "T-12", 4);

        table.occupy(99L, 3);

        assertEquals(TableStatus.OCCUPIED, table.getStatus());
        assertEquals(99L, table.getCurrentOrderId());
        assertEquals(3, table.getCovers());
    }

    @Test
    @DisplayName("Should throw TableAlreadyOccupiedException when occupying occupied table")
    void occupy_shouldThrowException_whenAlreadyOccupied() {
        HospitalityTable table = HospitalityTable.create(1L, 10L, 20L, "T-12", 4);
        table.occupy(99L, 3);

        assertThrows(TableAlreadyOccupiedException.class, () -> table.occupy(100L, 2));
    }

    @Test
    @DisplayName("Should throw exception when covers is zero or negative")
    void occupy_shouldThrowException_whenCoversInvalid() {
        HospitalityTable table = HospitalityTable.create(1L, 10L, 20L, "T-12", 4);

        assertThrows(InvalidOrderStateException.class, () -> table.occupy(99L, 0));
        assertThrows(InvalidOrderStateException.class, () -> table.occupy(99L, -1));
    }

    @Test
    @DisplayName("Should transition from OCCUPIED to BILL_REQUESTED")
    void requestBill_shouldUpdateStatus() {
        HospitalityTable table = HospitalityTable.create(1L, 10L, 20L, "T-12", 4);
        table.occupy(99L, 2);

        table.requestBill();

        assertEquals(TableStatus.BILL_REQUESTED, table.getStatus());
    }

    @Test
    @DisplayName("Should clear table back to AVAILABLE")
    void clear_shouldResetTable() {
        HospitalityTable table = HospitalityTable.create(1L, 10L, 20L, "T-12", 4);
        table.occupy(99L, 2);

        table.clear();

        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        assertNull(table.getCurrentOrderId());
    }
}
