package com.atlashub.commerce.storefront.application.commands.RefundPosSale;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.exceptions.SalesOrderNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundPosSaleHandlerTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private RefundPosSaleHandler handler;

    @Test
    @DisplayName("Should refund sales order and transition to REFUNDED")
    void execute_shouldRefundSalesOrder() {
        SalesOrderItem item = SalesOrderItem.create(
                1L, 100L, 10L, null, 1,
                Money.of(new BigDecimal("3000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
        SalesOrder order = SalesOrder.create(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, PaymentMethod.CASH, List.of(item), null
        );
        order.completePayment();
        when(salesOrderRepository.findById(100L)).thenReturn(Optional.of(order));

        handler.execute(new RefundPosSaleCommand(100L, "Customer returned item", "0123456789", "058"));

        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        verify(salesOrderRepository).save(order);
    }

    @Test
    @DisplayName("Should throw SalesOrderNotFoundException when order not found")
    void execute_shouldThrowException_whenNotFound() {
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SalesOrderNotFoundException.class, () ->
                handler.execute(new RefundPosSaleCommand(999L, "Damaged", null, null))
        );
    }
}
