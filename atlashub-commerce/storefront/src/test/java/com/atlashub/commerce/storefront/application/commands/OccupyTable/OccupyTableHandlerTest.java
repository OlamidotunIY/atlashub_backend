package com.atlashub.commerce.storefront.application.commands.OccupyTable;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OccupyTableHandlerTest {

    @Mock
    private HospitalityTableRepository tableRepository;

    @InjectMocks
    private OccupyTableHandler handler;

    @Test
    @DisplayName("Should occupy table and update table status")
    void execute_shouldOccupyTable() {
        HospitalityTable table = HospitalityTable.create(10L, 1L, 5L, "Table 1", 4);
        when(tableRepository.findById(10L)).thenReturn(Optional.of(table));

        handler.execute(new OccupyTableCommand(10L, 500L, 3));

        assertEquals(TableStatus.OCCUPIED, table.getStatus());
        assertEquals(500L, table.getCurrentOrderId());
        assertEquals(3, table.getCovers());
        verify(tableRepository).save(table);
    }

    @Test
    @DisplayName("Should throw TableNotFoundException when table not found")
    void execute_shouldThrowException_whenNotFound() {
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TableNotFoundException.class, () ->
                handler.execute(new OccupyTableCommand(999L, 500L, 2))
        );
    }
}
