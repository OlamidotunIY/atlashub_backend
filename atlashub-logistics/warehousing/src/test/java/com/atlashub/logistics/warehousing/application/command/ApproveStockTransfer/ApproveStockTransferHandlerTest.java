package com.atlashub.logistics.warehousing.application.command.ApproveStockTransfer;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.LocationType;
import com.atlashub.logistics.warehousing.domain.valueobject.StockTransferItem;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;
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
class ApproveStockTransferHandlerTest {

    @Mock
    private StockTransferRepository stockTransferRepository;

    private ApproveStockTransferHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApproveStockTransferHandler(stockTransferRepository);
    }

    @Test
    @DisplayName("Should approve requested stock transfer and transition status to APPROVED")
    void shouldApproveStockTransfer() {
        Address origin = new Address("Store A", "Ikeja", "Lagos", "Nigeria", "100001");
        Address dest = new Address("Store B", "Yaba", "Lagos", "Nigeria", "100002");
        StockTransferItem item = new StockTransferItem(501L, "SKU-501", "Widget", 10, new BigDecimal("1.0"));
        Money fee = Money.of(new BigDecimal("4000"), CurrencyCode.NGN);

        StockTransfer transfer = StockTransfer.create(
                1L, 100L, 10L, LocationType.OUTLET, origin,
                20L, LocationType.OUTLET, dest, fee, List.of(item), 55L, "corr-req-1"
        );

        when(stockTransferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        ApproveStockTransferCommand command = new ApproveStockTransferCommand(
                100L, 1L, 88L, Money.of(new BigDecimal("20000"), CurrencyCode.NGN), "corr-app-1"
        );

        ApproveStockTransferResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(1L, result.transferId());
        assertEquals("APPROVED", result.status());
        assertEquals(88L, result.approvedBy());

        verify(stockTransferRepository).save(any(StockTransfer.class));
    }
}
