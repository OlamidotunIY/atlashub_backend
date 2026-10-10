package com.atlashub.commerce.storefront.presentation.rest;

import com.atlashub.commerce.storefront.application.commands.AddCustomerDepositPayment.AddCustomerDepositPaymentCommand;
import com.atlashub.commerce.storefront.application.commands.AddCustomerDepositPayment.AddCustomerDepositPaymentHandler;
import com.atlashub.commerce.storefront.application.commands.CreateCustomerDeposit.CreateCustomerDepositCommand;
import com.atlashub.commerce.storefront.application.commands.CreateCustomerDeposit.CreateCustomerDepositHandler;
import com.atlashub.commerce.storefront.application.commands.CreateCustomerDeposit.CreateCustomerDepositResult;
import com.atlashub.commerce.storefront.application.commands.CreateOnlineOrder.CreateOnlineOrderCommand;
import com.atlashub.commerce.storefront.application.commands.CreateOnlineOrder.CreateOnlineOrderHandler;
import com.atlashub.commerce.storefront.application.commands.CreateOnlineOrder.CreateOnlineOrderResult;
import com.atlashub.commerce.storefront.application.queries.GetCustomerCredit.CustomerCreditResult;
import com.atlashub.commerce.storefront.application.queries.GetCustomerCredit.GetCustomerCreditHandler;
import com.atlashub.commerce.storefront.application.queries.GetCustomerCredit.GetCustomerCreditQuery;
import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.GetSalesOrderHandler;
import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.GetSalesOrderQuery;
import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.SalesOrderResult;
import com.atlashub.commerce.storefront.domain.valueobject.CreditStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.commerce.storefront.presentation.dto.AddCustomerDepositPaymentRequest;
import com.atlashub.commerce.storefront.presentation.dto.CreateCustomerDepositRequest;
import com.atlashub.commerce.storefront.presentation.dto.CreateCustomerDepositResponse;
import com.atlashub.commerce.storefront.presentation.dto.CreateOnlineOrderRequest;
import com.atlashub.commerce.storefront.presentation.dto.CreateOnlineOrderResponse;
import com.atlashub.commerce.storefront.presentation.dto.OrderItemRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceOnlineControllerTest {

    @Mock
    private CreateOnlineOrderHandler createOnlineOrderHandler;

    @Mock
    private CreateCustomerDepositHandler createCustomerDepositHandler;

    @Mock
    private AddCustomerDepositPaymentHandler addCustomerDepositPaymentHandler;

    @Mock
    private GetCustomerCreditHandler getCustomerCreditHandler;

    @Mock
    private GetSalesOrderHandler getSalesOrderHandler;

    @InjectMocks
    private CommerceOnlineController controller;

    private AuthenticatedPrincipal createPrincipal() {
        return new AuthenticatedPrincipal(
                1L,
                10L,
                "LIVE",
                "sess-123",
                "tok-123",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Should create online order and return 201")
    void createOnlineOrder_shouldReturn201() {
        AuthenticatedPrincipal principal = createPrincipal();
        CreateOnlineOrderRequest request = new CreateOnlineOrderRequest(
                20L,
                40L,
                List.of(new OrderItemRequest(100L, null, 1, new BigDecimal("3000.00"), CurrencyCode.NGN, null, null)),
                "123 Broad Street, Lagos",
                PaymentMethod.CARD
        );

        when(createOnlineOrderHandler.execute(any(CreateOnlineOrderCommand.class)))
                .thenReturn(new CreateOnlineOrderResult(999L));

        ResponseEntity<ApiResponse<CreateOnlineOrderResponse>> response =
                controller.createOnlineOrder(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(999L, response.getBody().data().salesOrderId());
        assertEquals(OrderStatus.PENDING, response.getBody().data().status());

        ArgumentCaptor<CreateOnlineOrderCommand> captor = ArgumentCaptor.forClass(CreateOnlineOrderCommand.class);
        verify(createOnlineOrderHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(20L, captor.getValue().outletId());
        assertEquals(40L, captor.getValue().customerId());
        assertEquals("123 Broad Street, Lagos", captor.getValue().deliveryAddress());
    }

    @Test
    @DisplayName("Should create customer deposit and return 201")
    void createDeposit_shouldReturn201() {
        CreateCustomerDepositRequest request = new CreateCustomerDepositRequest(
                888L,
                new BigDecimal("20000.00"),
                new BigDecimal("50000.00"),
                CurrencyCode.NGN
        );

        when(createCustomerDepositHandler.execute(any(CreateCustomerDepositCommand.class)))
                .thenReturn(new CreateCustomerDepositResult(333L));

        ResponseEntity<ApiResponse<CreateCustomerDepositResponse>> response =
                controller.createDeposit(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(333L, response.getBody().data().depositId());

        ArgumentCaptor<CreateCustomerDepositCommand> captor = ArgumentCaptor.forClass(CreateCustomerDepositCommand.class);
        verify(createCustomerDepositHandler).execute(captor.capture());
        assertEquals(888L, captor.getValue().salesOrderId());
    }

    @Test
    @DisplayName("Should add payment to customer deposit and return 200")
    void addDepositPayment_shouldReturn200() {
        AddCustomerDepositPaymentRequest request = new AddCustomerDepositPaymentRequest(
                new BigDecimal("10000.00"),
                CurrencyCode.NGN
        );

        ResponseEntity<ApiResponse<Void>> response = controller.addDepositPayment(333L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<AddCustomerDepositPaymentCommand> captor = ArgumentCaptor.forClass(AddCustomerDepositPaymentCommand.class);
        verify(addCustomerDepositPaymentHandler).execute(captor.capture());
        assertEquals(333L, captor.getValue().depositId());
    }

    @Test
    @DisplayName("Should get customer credit and return 200")
    void getCustomerCredit_shouldReturn200() {
        AuthenticatedPrincipal principal = createPrincipal();
        CustomerCreditResult creditResult = new CustomerCreditResult(
                555L,
                10L,
                40L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN),
                CreditStatus.WITHIN_LIMIT
        );

        when(getCustomerCreditHandler.execute(any(GetCustomerCreditQuery.class)))
                .thenReturn(creditResult);

        ResponseEntity<ApiResponse<CustomerCreditResult>> response = controller.getCustomerCredit(principal, 40L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(555L, response.getBody().data().id());

        ArgumentCaptor<GetCustomerCreditQuery> captor = ArgumentCaptor.forClass(GetCustomerCreditQuery.class);
        verify(getCustomerCreditHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(40L, captor.getValue().customerId());
    }

    @Test
    @DisplayName("Should get sales order and return 200")
    void getSalesOrder_shouldReturn200() {
        SalesOrderResult orderResult = new SalesOrderResult(
                888L,
                10L,
                20L,
                null,
                40L,
                1L,
                30L,
                OrderType.POS_RETAIL,
                OrderStatus.COMPLETED,
                Collections.emptyList(),
                null,
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                Money.of(BigDecimal.ZERO, CurrencyCode.NGN),
                Money.of(BigDecimal.ZERO, CurrencyCode.NGN),
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                PaymentMethod.CASH,
                null,
                ZonedDateTime.now(),
                null
        );

        when(getSalesOrderHandler.execute(any(GetSalesOrderQuery.class)))
                .thenReturn(orderResult);

        ResponseEntity<ApiResponse<SalesOrderResult>> response = controller.getSalesOrder(888L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(888L, response.getBody().data().id());

        ArgumentCaptor<GetSalesOrderQuery> captor = ArgumentCaptor.forClass(GetSalesOrderQuery.class);
        verify(getSalesOrderHandler).execute(captor.capture());
        assertEquals(888L, captor.getValue().salesOrderId());
    }
}
