package com.atlashub.commerce.inventory.application.queries.ListStockTransfers;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.repositories.StockTransferRepository;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListStockTransfersHandlerTest {

    private StockTransferRepository stockTransferRepository;
    private ListStockTransfersHandler handler;

    @BeforeEach
    void setUp() {
        stockTransferRepository = mock(StockTransferRepository.class);
        handler = new ListStockTransfersHandler(stockTransferRepository);
    }

    @Test
    @DisplayName("Should return paginated stock transfers with items")
    void shouldReturnPaginatedStockTransfers() {
        StockTransfer transfer = StockTransfer.create(1L, 10L, 20L, 30L);
        transfer.addItem(101L, 500L, 5);

        PageResult<StockTransfer> page = new PageResult<>(List.of(transfer), 0, 10, 1, 1);
        when(stockTransferRepository.findByOrganizationId(10L, TransferStatus.REQUESTED, 0, 10))
                .thenReturn(page);

        ListStockTransfersQuery query = new ListStockTransfersQuery(10L, TransferStatus.REQUESTED, 0, 10);
        PageResult<StockTransferResult> result = handler.execute(query);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(1L, result.content().get(0).id());
        assertEquals(1, result.content().get(0).items().size());
        assertEquals(500L, result.content().get(0).items().get(0).productId());
    }
}
