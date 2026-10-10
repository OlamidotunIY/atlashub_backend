package com.atlashub.commerce.inventory.application.queries.GetInventoryLevel;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.exceptions.InventoryNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetInventoryLevelHandlerTest {

    private InventoryRepository inventoryRepository;
    private GetInventoryLevelHandler handler;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        handler = new GetInventoryLevelHandler(inventoryRepository);
    }

    @Test
    @DisplayName("Should return inventory level when found")
    void shouldGetInventoryLevelSuccessfully() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 100L, null, 50, 10, 5);
        when(inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(10L, 20L, 100L))
                .thenReturn(Optional.of(inventory));

        GetInventoryLevelQuery query = new GetInventoryLevelQuery(10L, 20L, 100L, null);
        InventoryResult result = handler.execute(query);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(50, result.quantity());
        assertEquals(0, result.reservedQuantity());
        assertEquals(50, result.availableQuantity());
    }

    @Test
    @DisplayName("Should throw InventoryNotFoundException when inventory is missing")
    void shouldThrowWhenInventoryNotFound() {
        when(inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(10L, 20L, 100L))
                .thenReturn(Optional.empty());

        GetInventoryLevelQuery query = new GetInventoryLevelQuery(10L, 20L, 100L, null);
        assertThrows(InventoryNotFoundException.class, () -> handler.execute(query));
    }
}
