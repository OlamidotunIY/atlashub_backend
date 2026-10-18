package com.atlashub.pay.mandates.application.commands.ChargeMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.events.MandateChargeDueEvent;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargeMandateHandlerTest {

    @Mock
    private PaymentMandateRepository mandateRepository;

    private ChargeMandateHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ChargeMandateHandler(mandateRepository);
    }

    @Test
    @DisplayName("Should execute charge on active mandate and advance date")
    void shouldChargeActiveMandate() {
        PaymentMandate mandate = PaymentMandate.create(
                1L, 10L, "CUST-1", new EmailAddress("user@example.com"),
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                MandateFrequency.MONTHLY, "AUTH", LocalDate.of(2026, 11, 1)
        );

        when(mandateRepository.findById(1L)).thenReturn(Optional.of(mandate));

        handler.execute(new ChargeMandateCommand(1L));

        assertEquals(LocalDate.of(2026, 12, 1), mandate.getNextChargeDate());
        assertTrue(mandate.peekDomainEvents().stream().anyMatch(e -> e instanceof MandateChargeDueEvent));
        verify(mandateRepository).save(mandate);
    }

    @Test
    @DisplayName("Should skip charge when mandate is not active")
    void shouldSkipWhenNotActive() {
        PaymentMandate mandate = PaymentMandate.create(
                1L, 10L, "CUST-1", new EmailAddress("user@example.com"),
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                MandateFrequency.MONTHLY, "AUTH", LocalDate.of(2026, 11, 1)
        );
        mandate.pause();

        when(mandateRepository.findById(1L)).thenReturn(Optional.of(mandate));

        handler.execute(new ChargeMandateCommand(1L));

        verify(mandateRepository, never()).save(mandate);
    }

    @Test
    @DisplayName("Should throw MandateNotFoundException when mandate does not exist")
    void shouldThrowWhenNotFound() {
        when(mandateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MandateNotFoundException.class, () -> handler.execute(new ChargeMandateCommand(99L)));
    }
}
