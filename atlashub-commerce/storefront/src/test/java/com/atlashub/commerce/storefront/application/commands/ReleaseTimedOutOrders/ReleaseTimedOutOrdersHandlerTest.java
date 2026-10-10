package com.atlashub.commerce.storefront.application.commands.ReleaseTimedOutOrders;

import com.atlashub.commerce.storefront.application.commands.FailPayment.FailPaymentCommand;
import com.atlashub.commerce.storefront.application.commands.FailPayment.FailPaymentHandler;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseTimedOutOrdersHandlerTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private FailPaymentHandler failPaymentHandler;

    @InjectMocks
    private ReleaseTimedOutOrdersHandler handler;

    @Test
    @DisplayName("Should find timed out orders and invoke FailPaymentHandler for each")
    void execute_shouldFailTimedOutOrders() {
        SalesOrderItem item = SalesOrderItem.create(
                1L, 100L, 10L, null, 1,
                Money.of(new BigDecimal("3000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
        SalesOrder order = SalesOrder.create(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, PaymentMethod.CARD, List.of(item), null
        );
        order.initiatePayment("chg-ref-123");

        when(salesOrderRepository.findByStatusAndSaleDateBefore(eq(OrderStatus.PAYMENT_PENDING), any()))
                .thenReturn(List.of(order));

        handler.execute(new ReleaseTimedOutOrdersCommand(15));

        verify(failPaymentHandler).execute(new FailPaymentCommand(100L, "Payment timed out"));
    }
}
