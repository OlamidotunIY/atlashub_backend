package com.atlashub.commerce.storefront.application.queries.ListActiveTables;

import com.atlashub.commerce.storefront.domain.entities.HospitalityTable;
import com.atlashub.commerce.storefront.domain.repositories.HospitalityTableRepository;
import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListActiveTablesHandlerTest {

    @Mock
    private HospitalityTableRepository tableRepository;

    @InjectMocks
    private ListActiveTablesHandler handler;

    @Test
    @DisplayName("Should return list of tables for outlet")
    void execute_shouldReturnTables() {
        HospitalityTable table1 = HospitalityTable.create(1L, 10L, 5L, "Table 1", 4);
        HospitalityTable table2 = HospitalityTable.create(2L, 10L, 5L, "Table 2", 2);
        table2.occupy(500L, 2);

        when(tableRepository.findByOutletId(5L)).thenReturn(List.of(table1, table2));

        List<TableResult> results = handler.execute(new ListActiveTablesQuery(5L));

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Table 1", results.get(0).tableNumber());
        assertEquals(TableStatus.AVAILABLE, results.get(0).status());
        assertEquals("Table 2", results.get(1).tableNumber());
        assertEquals(TableStatus.OCCUPIED, results.get(1).status());
    }
}
