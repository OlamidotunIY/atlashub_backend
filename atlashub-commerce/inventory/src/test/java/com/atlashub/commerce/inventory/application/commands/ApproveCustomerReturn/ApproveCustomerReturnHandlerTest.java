package com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn;

import com.atlashub.commerce.inventory.domain.entities.CustomerReturn;
import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.exceptions.CustomerReturnNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.CustomerReturnRepository;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.valueobject.RefundMethod;
import com.atlashub.commerce.inventory.domain.valueobject.ReturnStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApproveCustomerReturnHandlerTest {

    private CustomerReturnRepository customerReturnRepository;
    private InventoryRepository inventoryRepository;
    private ApproveCustomerReturnHandler handler;

    @BeforeEach
    void setUp() {
        customerReturnRepository = mock(CustomerReturnRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        handler = new ApproveCustomerReturnHandler(customerReturnRepository, inventoryRepository);
    }

    @Test
    @DisplayName("Should approve customer return and restore stock at outlet")
    void shouldApproveCustomerReturnAndRestoreStock() {
        CustomerReturn customerReturn = CustomerReturn.create(
                1L, 10L, 20L, 30L, 40L,
                Money.of(BigDecimal.valueOf(1000), CurrencyCode.NGN),
                "Damaged",
                RefundMethod.CASH
        );
        customerReturn.addItem(101L, 500L, 2);

        Inventory inventory = Inventory.create(5L, 10L, 20L, 500L, null, 10, 10, 5);

        when(customerReturnRepository.findById(1L)).thenReturn(Optional.of(customerReturn));
        when(inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(10L, 20L, 500L))
                .thenReturn(Optional.of(inventory));

        ApproveCustomerReturnCommand command = new ApproveCustomerReturnCommand(1L);
        Void result = handler.execute(command);

        assertNull(result);
        assertEquals(ReturnStatus.APPROVED, customerReturn.getStatus());
        assertEquals(12, inventory.getQuantity());

        verify(inventoryRepository).save(inventory);
        verify(customerReturnRepository).save(customerReturn);
    }

    @Test
    @DisplayName("Should throw CustomerReturnNotFoundException when return is missing")
    void shouldThrowWhenReturnNotFound() {
        when(customerReturnRepository.findById(1L)).thenReturn(Optional.empty());

        ApproveCustomerReturnCommand command = new ApproveCustomerReturnCommand(1L);
        assertThrows(CustomerReturnNotFoundException.class, () -> handler.execute(command));
    }
}
