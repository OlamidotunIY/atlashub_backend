package com.atlashub.commerce.storefront.application.commands.CloseTill;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseTillHandlerTest {

    @Mock
    private TillRepository tillRepository;

    @InjectMocks
    private CloseTillHandler handler;

    @Test
    @DisplayName("Should close till and transition status to CLOSED")
    void execute_shouldCloseTill() {
        Money openingFloat = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Till till = Till.create(200L, 1L, 10L, "Main Register", 50L, openingFloat);
        when(tillRepository.findById(200L)).thenReturn(Optional.of(till));

        Money actualClosingBalance = Money.of(new BigDecimal("15000.00"), CurrencyCode.NGN);
        handler.execute(new CloseTillCommand(200L, 50L, actualClosingBalance));

        assertEquals(TillStatus.CLOSED, till.getStatus());
        assertEquals(actualClosingBalance, till.getActualClosingBalance());
        verify(tillRepository).save(till);
    }

    @Test
    @DisplayName("Should throw TillNotFoundException when till does not exist")
    void execute_shouldThrowException_whenNotFound() {
        when(tillRepository.findById(999L)).thenReturn(Optional.empty());

        Money actualClosingBalance = Money.of(new BigDecimal("15000.00"), CurrencyCode.NGN);
        assertThrows(TillNotFoundException.class, () ->
                handler.execute(new CloseTillCommand(999L, 50L, actualClosingBalance))
        );
    }
}
