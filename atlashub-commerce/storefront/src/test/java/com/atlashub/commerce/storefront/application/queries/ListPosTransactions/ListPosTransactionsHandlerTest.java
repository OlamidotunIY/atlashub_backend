package com.atlashub.commerce.storefront.application.queries.ListPosTransactions;

import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.SalesOrderResult;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPosTransactionsHandlerTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private ListPosTransactionsHandler handler;

    @Test
    @DisplayName("Should return paged sales order results")
    void execute_shouldReturnPagedResults() {
        SalesOrderItem item = SalesOrderItem.create(
                1L, 100L, 10L, null, 1,
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
        SalesOrder order = SalesOrder.create(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, PaymentMethod.CASH, List.of(item), null
        );

        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now();
        ListPosTransactionsQuery query = new ListPosTransactionsQuery(20L, 30L, 50L, from, to, 0, 10);

        PageResult<SalesOrder> paged = new PageResult<>(List.of(order), 0, 10, 1L, 1);
        when(salesOrderRepository.findTransactions(20L, 30L, 50L, from, to, 0, 10)).thenReturn(paged);

        PageResult<SalesOrderResult> result = handler.execute(query);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(100L, result.content().getFirst().id());
        assertEquals(1L, result.totalElements());
    }
}
