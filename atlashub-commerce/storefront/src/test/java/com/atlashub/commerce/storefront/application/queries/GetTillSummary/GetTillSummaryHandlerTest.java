package com.atlashub.commerce.storefront.application.queries.GetTillSummary;

import com.atlashub.commerce.storefront.domain.entities.Till;
import com.atlashub.commerce.storefront.domain.exceptions.TillNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.TillRepository;
import com.atlashub.commerce.storefront.domain.valueobject.TillStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTillSummaryHandlerTest {

    @Mock
    private TillRepository tillRepository;

    @InjectMocks
    private GetTillSummaryHandler handler;

    @Test
    @DisplayName("Should return till summary when till exists")
    void execute_shouldReturnTillSummary() {
        Money openingFloat = Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN);
        Till till = Till.create(200L, 1L, 10L, "Main Till", 50L, openingFloat);
        when(tillRepository.findById(200L)).thenReturn(Optional.of(till));

        TillSummaryResult result = handler.execute(new GetTillSummaryQuery(200L));

        assertNotNull(result);
        assertEquals(200L, result.tillId());
        assertEquals("Main Till", result.name());
        assertEquals(TillStatus.OPEN, result.status());
        assertEquals(openingFloat, result.openingFloat());
    }

    @Test
    @DisplayName("Should throw TillNotFoundException when till does not exist")
    void execute_shouldThrowException_whenNotFound() {
        when(tillRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TillNotFoundException.class, () ->
                handler.execute(new GetTillSummaryQuery(999L))
        );
    }
}
