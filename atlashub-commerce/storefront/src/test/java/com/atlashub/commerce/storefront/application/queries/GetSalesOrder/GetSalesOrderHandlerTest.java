package com.atlashub.commerce.storefront.application.queries.GetSalesOrder;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.exceptions.SalesOrderNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSalesOrderHandlerTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private GetSalesOrderHandler handler;

    @Test
    @DisplayName("Should return sales order details when order exists")
    void execute_shouldReturnSalesOrder() {
        SalesOrderItem item = SalesOrderItem.create(
                1L, 100L, 10L, null, 2,
                Money.of(new BigDecimal("2500.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
        SalesOrder order = SalesOrder.create(
                100L, 1L, 20L, null, 15L, 50L, 30L,
                OrderType.POS_RETAIL, PaymentMethod.CASH, List.of(item), null
        );
        when(salesOrderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        SalesOrderResult result = handler.execute(new GetSalesOrderQuery(100L));

        assertNotNull(result);
        assertEquals(100L, result.id());
        assertEquals(1, result.items().size());
        assertEquals(10L, result.items().getFirst().productId());
    }

    @Test
    @DisplayName("Should throw SalesOrderNotFoundException when order does not exist")
    void execute_shouldThrowException_whenNotFound() {
        when(salesOrderRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SalesOrderNotFoundException.class, () ->
                handler.execute(new GetSalesOrderQuery(999L))
        );
    }
}
