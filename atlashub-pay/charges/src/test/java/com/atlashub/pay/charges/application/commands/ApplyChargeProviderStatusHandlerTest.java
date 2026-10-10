package com.atlashub.pay.charges.application.commands;

import com.atlashub.pay.charges.application.commands.ApplyChargeProviderStatus.ApplyChargeProviderStatusCommand;
import com.atlashub.pay.charges.application.commands.ApplyChargeProviderStatus.ApplyChargeProviderStatusHandler;
import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.exceptions.ChargeNotFoundException;
import com.atlashub.pay.charges.domain.exceptions.InvalidChargeException;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
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
class ApplyChargeProviderStatusHandlerTest {

    @Mock
    private ChargeRepository chargeRepository;
    @Mock
    private ChargeProviderPort providerPort;

    @InjectMocks
    private ApplyChargeProviderStatusHandler handler;

    private Charge createPendingCharge() {
        Charge charge = Charge.initialize(
                1L, 10L, ApiEnvironment.TEST, "REF-123",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD, 20L, "COMMERCE", "ORD-1"
        );
        charge.markPending("PAYSTACK-REF-123", "https://pay.com", "ACCESS-123");
        return charge;
    }

    @Test
    @DisplayName("Should successfully transition charge to SUCCESSFUL and save")
    void shouldApplySuccessfulStatus() {
        Charge charge = createPendingCharge();
        when(chargeRepository.findByProviderReferenceAndEnvironment("PAYSTACK-REF-123", ApiEnvironment.TEST))
                .thenReturn(Optional.of(charge));
        when(providerPort.fetchStatus("PAYSTACK-REF-123", ApiEnvironment.TEST)).thenReturn(
                new ChargeProviderPort.ProviderStatus("PAYSTACK-REF-123", "success",
                        new BigDecimal("5000.00"), CurrencyCode.NGN, "card"));

        ApplyChargeProviderStatusCommand command = new ApplyChargeProviderStatusCommand(
                ApiEnvironment.TEST,
                "REF-123",
                "PAYSTACK-REF-123",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                CurrencyCode.NGN,
                true,
                null
        );

        handler.execute(command);

        assertEquals(ChargeStatus.SUCCESSFUL, charge.getStatus());
        verify(chargeRepository).save(charge);
    }

    @Test
    @DisplayName("Should transition charge to FAILED when provider reports failure")
    void shouldApplyFailedStatus() {
        Charge charge = createPendingCharge();
        when(chargeRepository.findByProviderReferenceAndEnvironment("PAYSTACK-REF-123", ApiEnvironment.TEST))
                .thenReturn(Optional.of(charge));
        when(providerPort.fetchStatus("PAYSTACK-REF-123", ApiEnvironment.TEST)).thenReturn(
                new ChargeProviderPort.ProviderStatus("PAYSTACK-REF-123", "failed",
                        new BigDecimal("5000.00"), CurrencyCode.NGN, "card"));

        ApplyChargeProviderStatusCommand command = new ApplyChargeProviderStatusCommand(
                ApiEnvironment.TEST,
                "REF-123",
                "PAYSTACK-REF-123",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                CurrencyCode.NGN,
                false,
                "Card declined"
        );

        handler.execute(command);

        assertEquals(ChargeStatus.FAILED, charge.getStatus());
        assertEquals("Card declined", charge.getFailureMessage());
        verify(chargeRepository).save(charge);
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when charge is not found")
    void shouldThrowWhenChargeNotFound() {
        when(chargeRepository.findByProviderReferenceAndEnvironment("UNKNOWN-REF", ApiEnvironment.TEST))
                .thenReturn(Optional.empty());

        ApplyChargeProviderStatusCommand command = new ApplyChargeProviderStatusCommand(
                ApiEnvironment.TEST,
                null,
                "UNKNOWN-REF",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                CurrencyCode.NGN,
                true,
                null
        );

        assertThrows(ChargeNotFoundException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should throw InvalidChargeException when confirmed amount mismatches")
    void shouldThrowWhenAmountMismatches() {
        Charge charge = createPendingCharge();
        when(chargeRepository.findByProviderReferenceAndEnvironment("PAYSTACK-REF-123", ApiEnvironment.TEST))
                .thenReturn(Optional.of(charge));
        when(providerPort.fetchStatus("PAYSTACK-REF-123", ApiEnvironment.TEST)).thenReturn(
                new ChargeProviderPort.ProviderStatus("PAYSTACK-REF-123", "success",
                        new BigDecimal("1000.00"), CurrencyCode.NGN, "card"));

        ApplyChargeProviderStatusCommand command = new ApplyChargeProviderStatusCommand(
                ApiEnvironment.TEST,
                "REF-123",
                "PAYSTACK-REF-123",
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                CurrencyCode.NGN,
                true,
                null
        );

        assertThrows(InvalidChargeException.class, () -> handler.execute(command));
    }
}
