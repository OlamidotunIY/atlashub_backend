package com.atlashub.commerce.storefront.application.commands.OpenTill;

import com.atlashub.commerce.storefront.domain.entities.Till;
import com.atlashub.commerce.storefront.domain.exceptions.TillAlreadyOpenException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenTillHandlerTest {

    @Mock
    private TillRepository tillRepository;

    @InjectMocks
    private OpenTillHandler handler;

    @Test
    @DisplayName("Should open till when no active till exists for outlet")
    void execute_shouldOpenTill() {
        Money openingFloat = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        OpenTillCommand command = new OpenTillCommand(1L, 10L, "Main Register", 50L, openingFloat);

        when(tillRepository.findActiveTillByOutletId(10L)).thenReturn(Optional.empty());
        when(tillRepository.nextIdentity()).thenReturn(200L);
        when(tillRepository.save(any(Till.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OpenTillResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(200L, result.tillId());
        verify(tillRepository).save(any(Till.class));
    }

    @Test
    @DisplayName("Should throw TillAlreadyOpenException when active till exists")
    void execute_shouldThrowException_whenTillAlreadyOpen() {
        Money openingFloat = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Till existingTill = Till.create(200L, 1L, 10L, "Main Register", 50L, openingFloat);
        when(tillRepository.findActiveTillByOutletId(10L)).thenReturn(Optional.of(existingTill));

        OpenTillCommand command = new OpenTillCommand(1L, 10L, "Second Register", 50L, openingFloat);

        assertThrows(TillAlreadyOpenException.class, () -> handler.execute(command));
    }
}
