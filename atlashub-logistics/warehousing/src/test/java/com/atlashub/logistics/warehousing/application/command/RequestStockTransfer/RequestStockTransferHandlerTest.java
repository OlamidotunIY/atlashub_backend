package com.atlashub.logistics.warehousing.application.command.RequestStockTransfer;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.LocationType;
import com.atlashub.logistics.warehousing.domain.valueobject.StockTransferItem;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;
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
class RequestStockTransferHandlerTest {

    @Mock
    private StockTransferRepository stockTransferRepository;

    private RequestStockTransferHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RequestStockTransferHandler(stockTransferRepository);
    }

    @Test
    @DisplayName("Should successfully request stock transfer")
    void shouldRequestStockTransfer() {
        when(stockTransferRepository.nextIdentity()).thenReturn(301L);

        Address origin = new Address("Store A", "Ikeja", "Lagos", "Nigeria", "100001");
        Address dest = new Address("Store B", "Yaba", "Lagos", "Nigeria", "100002");
        StockTransferItem item = new StockTransferItem(501L, "SKU-501", "Widget", 20, new BigDecimal("1.5"));
        Money fee = Money.of(new BigDecimal("5000"), CurrencyCode.NGN);

        RequestStockTransferCommand command = new RequestStockTransferCommand(
                100L,
                10L,
                LocationType.OUTLET,
                origin,
                20L,
                LocationType.OUTLET,
                dest,
                fee,
                List.of(item),
                55L,
                "corr-req-1"
        );

        RequestStockTransferResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(301L, result.transferId());
        assertEquals("REQUESTED", result.status());
        assertEquals(0, new BigDecimal("30.0").compareTo(result.totalWeightKg()));
        assertEquals(fee, result.transferShippingFee());

        verify(stockTransferRepository).save(any(StockTransfer.class));
    }
}
