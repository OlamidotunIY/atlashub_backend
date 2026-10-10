package com.atlashub.commerce.catalog.presentation.rest;

import com.atlashub.commerce.catalog.application.commands.CreateDiscount.CreateDiscountCommand;
import com.atlashub.commerce.catalog.application.commands.CreateDiscount.CreateDiscountHandler;
import com.atlashub.commerce.catalog.application.commands.CreateDiscount.CreateDiscountResult;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountScope;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountType;
import com.atlashub.commerce.catalog.presentation.dto.CreateDiscountRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreateDiscountResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountControllerTest {

    @Mock
    private CreateDiscountHandler createDiscountHandler;

    @InjectMocks
    private DiscountController controller;

    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedPrincipal(
                1L,
                10L,
                "LIVE",
                "sess-1",
                "tok-1",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Should create discount and return 201 Created")
    void shouldCreateDiscount() {
        CreateDiscountRequest request = new CreateDiscountRequest(
                "Promo 2026",
                DiscountType.PERCENTAGE,
                new BigDecimal("10.00"),
                DiscountScope.ORDER_LEVEL,
                new BigDecimal("500.00"),
                50,
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );

        when(createDiscountHandler.execute(any(CreateDiscountCommand.class)))
                .thenReturn(new CreateDiscountResult(99L));

        ResponseEntity<ApiResponse<CreateDiscountResponse>> response = controller.createDiscount(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(99L, response.getBody().data().discountId());

        ArgumentCaptor<CreateDiscountCommand> captor = ArgumentCaptor.forClass(CreateDiscountCommand.class);
        verify(createDiscountHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals("Promo 2026", captor.getValue().name());
    }
}
