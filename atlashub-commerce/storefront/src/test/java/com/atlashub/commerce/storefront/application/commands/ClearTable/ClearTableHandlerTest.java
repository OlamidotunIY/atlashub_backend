package com.atlashub.commerce.storefront.application.commands.ClearTable;

import com.atlashub.commerce.storefront.domain.entities.HospitalityTable;
import com.atlashub.commerce.storefront.domain.exceptions.TableNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.HospitalityTableRepository;
import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClearTableHandlerTest {

    @Mock
    private HospitalityTableRepository tableRepository;

    @InjectMocks
    private ClearTableHandler handler;

    @Test
    @DisplayName("Should clear table and reset status to AVAILABLE")
    void execute_shouldClearTable() {
        HospitalityTable table = HospitalityTable.create(10L, 1L, 5L, "Table 1", 4);
        table.occupy(500L, 2);
        when(tableRepository.findById(10L)).thenReturn(Optional.of(table));

        handler.execute(new ClearTableCommand(10L));

        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        assertNull(table.getCurrentOrderId());
        verify(tableRepository).save(table);
    }

    @Test
    @DisplayName("Should throw TableNotFoundException when table not found")
    void execute_shouldThrowException_whenNotFound() {
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TableNotFoundException.class, () ->
                handler.execute(new ClearTableCommand(999L))
        );
    }
}
