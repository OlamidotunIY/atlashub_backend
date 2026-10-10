package com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class ProcessPosCheckoutHandlerTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private ProcessPosCheckoutHandler handler;

    @Test
    @DisplayName("Should create and save sales order with PENDING status")
    void execute_shouldCreateAndSaveOrder() {
        when(salesOrderRepository.nextIdentity()).thenReturn(100L);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItemDto itemDto = new OrderItemDto(
                10L,
                null,
                2,
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );

        ProcessPosCheckoutCommand command = new ProcessPosCheckoutCommand(
                1L,
                20L,
                30L,
                40L,
                OrderType.POS_RETAIL,
                List.of(itemDto),
                null,
                PaymentMethod.CARD,
                50L
        );

        ProcessPosCheckoutResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(100L, result.salesOrderId());
        assertEquals(OrderStatus.PENDING, result.status());

        ArgumentCaptor<SalesOrder> captor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(salesOrderRepository).save(captor.capture());
        SalesOrder saved = captor.getValue();
        assertEquals(100L, saved.getId());
        assertEquals(1L, saved.getOrganizationId());
        assertEquals(20L, saved.getOutletId());
        assertEquals(1, saved.getItems().size());
        assertEquals(0, saved.getTotalGross().amount().compareTo(new BigDecimal("10000.00")));
    }
}
