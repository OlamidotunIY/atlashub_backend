package com.atlashub.pay.charges.application.commands;

import com.atlashub.pay.charges.application.commands.RefundCharge.RefundChargeCommand;
import com.atlashub.pay.charges.application.commands.RefundCharge.RefundChargeHandler;
import com.atlashub.pay.charges.application.commands.RefundCharge.RefundChargeResult;
import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.exceptions.ChargeNotFoundException;
import com.atlashub.pay.charges.domain.exceptions.InvalidChargeException;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.port.ChargeProviderPort;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundChargeHandlerTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private ChargeProviderPort provider;

    @InjectMocks
    private RefundChargeHandler handler;

    private Charge createSuccessfulCharge() {
        Charge charge = Charge.initialize(
                100L, 10L, ApiEnvironment.TEST, "REF-100",
                Money.of(new BigDecimal("2500.00"), CurrencyCode.NGN),
                ChargeChannel.CARD, 20L, "COMMERCE", "ORD-1"
        );
        charge.markPending("PAYSTACK-REF-100", "https://pay.com", "ACCESS-100");
        charge.succeed("PAYSTACK-REF-100", Money.of(new BigDecimal("2500.00"), CurrencyCode.NGN), CurrencyCode.NGN);
        return charge;
    }

    @Test
    @DisplayName("Should initiate refund on successful charge")
    void shouldInitiateRefundSuccessfully() {
        Charge charge = createSuccessfulCharge();
        when(chargeRepository.findById(100L)).thenReturn(Optional.of(charge));
        when(chargeRepository.save(charge)).thenReturn(charge);
        when(provider.refund(org.mockito.ArgumentMatchers.any())).thenReturn(
                new ChargeProviderPort.RefundResult("refund-100", "pending"));

        RefundChargeCommand command = new RefundChargeCommand(
                100L,
                10L,
                ApiEnvironment.TEST,
                "Customer requested order cancellation"
        );

        RefundChargeResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(100L, result.chargeId());
        assertEquals("REF-100", result.reference());
        assertEquals(ChargeStatus.REFUND_PENDING, result.status());
        assertEquals("Customer requested order cancellation", result.reason());

        verify(chargeRepository, org.mockito.Mockito.times(2)).save(charge);
        verify(provider).refund(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when charge ID is not found")
    void shouldThrowWhenChargeNotFound() {
        when(chargeRepository.findById(999L)).thenReturn(Optional.empty());

        RefundChargeCommand command = new RefundChargeCommand(
                999L,
                10L,
                ApiEnvironment.TEST,
                "Reason"
        );

        assertThrows(ChargeNotFoundException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when organizationId mismatches")
    void shouldThrowWhenOrganizationMismatches() {
        Charge charge = createSuccessfulCharge(); // org = 10L
        when(chargeRepository.findById(100L)).thenReturn(Optional.of(charge));

        RefundChargeCommand command = new RefundChargeCommand(
                100L,
                999L, // wrong org
                ApiEnvironment.TEST,
                "Reason"
        );

        assertThrows(ChargeNotFoundException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when environment mismatches")
    void shouldThrowWhenEnvironmentMismatches() {
        Charge charge = createSuccessfulCharge(); // env = TEST
        when(chargeRepository.findById(100L)).thenReturn(Optional.of(charge));

        RefundChargeCommand command = new RefundChargeCommand(
                100L,
                10L,
                ApiEnvironment.LIVE, // wrong env
                "Reason"
        );

        assertThrows(ChargeNotFoundException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should throw InvalidChargeException when charge is not in SUCCESSFUL status")
    void shouldThrowWhenChargeNotSuccessful() {
        Charge charge = Charge.initialize(
                101L, 10L, ApiEnvironment.TEST, "REF-101",
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD, 20L, "COMMERCE", "ORD-2"
        ); // Status = INITIALIZED
        when(chargeRepository.findById(101L)).thenReturn(Optional.of(charge));

        RefundChargeCommand command = new RefundChargeCommand(
                101L,
                10L,
                ApiEnvironment.TEST,
                "Reason"
        );

        assertThrows(InvalidChargeException.class, () -> handler.execute(command));
    }
}
