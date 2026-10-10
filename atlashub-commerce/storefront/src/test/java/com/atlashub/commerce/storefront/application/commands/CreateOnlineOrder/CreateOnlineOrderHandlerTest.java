package com.atlashub.commerce.storefront.application.commands.CreateOnlineOrder;

import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.OrderItemDto;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOnlineOrderHandlerTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private CreateOnlineOrderHandler handler;

    @Test
    @DisplayName("Should create online sales order")
    void execute_shouldCreateOnlineOrder() {
        OrderItemDto itemDto = new OrderItemDto(
                10L, null, 2,
                Money.of(new BigDecimal("4000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
        CreateOnlineOrderCommand command = new CreateOnlineOrderCommand(
                1L, 20L, 50L, List.of(itemDto), "123 Marina, Lagos", PaymentMethod.CARD
        );

        when(salesOrderRepository.nextIdentity()).thenReturn(800L);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOnlineOrderResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(800L, result.salesOrderId());
        verify(salesOrderRepository).save(any(SalesOrder.class));
    }
}
