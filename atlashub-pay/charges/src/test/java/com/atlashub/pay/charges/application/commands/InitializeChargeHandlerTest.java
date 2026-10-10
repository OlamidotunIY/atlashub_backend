package com.atlashub.pay.charges.application.commands;

import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeCommand;
import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeHandler;
import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeResult;
import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.exceptions.ChargeProviderException;
import com.atlashub.pay.charges.domain.exceptions.InvalidChargeException;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.shared.application.port.PaymentProviderProfileQueryPort;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitializeChargeHandlerTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private PaymentProviderProfileQueryPort providerProfileQueryPort;

    @Mock
    private ChargeProviderPort chargeProviderPort;

    @InjectMocks
    private InitializeChargeHandler handler;

    @Test
    @DisplayName("Should initialize charge and return checkout instructions")
    void shouldInitializeChargeSuccessfully() {
        InitializeChargeCommand command = new InitializeChargeCommand(
                10L,
                ApiEnvironment.TEST,
                "REF-CHG-1",
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD,
                "customer@example.com",
                "COMMERCE",
                "ORDER-101",
                "CUST-001",
                null,
                Map.of("metaKey", "metaVal")
        );

        when(chargeRepository.findByOrganizationIdAndEnvironmentAndReference(10L, ApiEnvironment.TEST, "REF-CHG-1"))
                .thenReturn(Optional.empty());

        PaymentProviderProfileQueryPort.ProviderProfile profile = new PaymentProviderProfileQueryPort.ProviderProfile(
                100L,
                10L,
                ApiEnvironment.TEST,
                "PAYSTACK",
                "ACCT_12345",
                "SETTLE_REF_1",
                Set.of("CARD_COLLECTION")
        );
        when(providerProfileQueryPort.findActiveProfile(10L, ApiEnvironment.TEST, "CARD_COLLECTION"))
                .thenReturn(Optional.of(profile));

        when(chargeRepository.nextIdentity()).thenReturn(999L);

        ChargeProviderPort.InitializationResult initResult = new ChargeProviderPort.InitializationResult(
                "PROV-REF-999",
                "https://checkout.paystack.com/auth",
                "ACCESS-999"
        );
        when(chargeProviderPort.initialize(any())).thenReturn(initResult);

        when(chargeRepository.save(any(Charge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InitializeChargeResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(999L, result.chargeId());
        assertEquals(10L, result.organizationId());
        assertEquals(ApiEnvironment.TEST, result.environment());
        assertEquals("REF-CHG-1", result.reference());
        assertEquals(ChargeStatus.PENDING, result.status());
        assertEquals("PROV-REF-999", result.providerReference());
        assertEquals("https://checkout.paystack.com/auth", result.authorizationUrl());
        assertEquals("ACCESS-999", result.accessCode());

        verify(chargeRepository, org.mockito.Mockito.times(2)).save(any(Charge.class));
    }

    @Test
    @DisplayName("Should return existing charge result when reference is duplicate (idempotent)")
    void shouldReturnExistingChargeWhenDuplicate() {
        InitializeChargeCommand command = new InitializeChargeCommand(
                10L,
                ApiEnvironment.TEST,
                "REF-CHG-1",
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD,
                "customer@example.com",
                "COMMERCE",
                "ORDER-101",
                null,
                null,
                null
        );

        Charge existing = Charge.initialize(
                555L, 10L, ApiEnvironment.TEST, "REF-CHG-1",
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD, 100L, "COMMERCE", "ORDER-101"
        );
        existing.markPending("EXISTING-PROV-REF", "https://existing.url", "EXISTING-CODE");

        when(chargeRepository.findByOrganizationIdAndEnvironmentAndReference(10L, ApiEnvironment.TEST, "REF-CHG-1"))
                .thenReturn(Optional.of(existing));

        InitializeChargeResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(555L, result.chargeId());
        assertEquals(ChargeStatus.PENDING, result.status());
        assertEquals("EXISTING-PROV-REF", result.providerReference());

        verify(providerProfileQueryPort, never()).findActiveProfile(any(), any(), any());
        verify(chargeProviderPort, never()).initialize(any());
        verify(chargeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidChargeException when capability profile is not enabled")
    void shouldThrowWhenCapabilityNotEnabled() {
        InitializeChargeCommand command = new InitializeChargeCommand(
                10L,
                ApiEnvironment.LIVE,
                "REF-CHG-1",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD,
                "customer@example.com",
                "COMMERCE",
                "ORDER-101",
                null,
                null,
                null
        );

        when(chargeRepository.findByOrganizationIdAndEnvironmentAndReference(10L, ApiEnvironment.LIVE, "REF-CHG-1"))
                .thenReturn(Optional.empty());

        when(providerProfileQueryPort.findActiveProfile(10L, ApiEnvironment.LIVE, "CARD_COLLECTION"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidChargeException.class, () -> handler.execute(command));
        verify(chargeProviderPort, never()).initialize(any());
        verify(chargeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should propagate ChargeProviderException when provider fails")
    void shouldPropagateExceptionWhenProviderFails() {
        InitializeChargeCommand command = new InitializeChargeCommand(
                10L,
                ApiEnvironment.TEST,
                "REF-CHG-1",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                ChargeChannel.USSD,
                "customer@example.com",
                "COMMERCE",
                "ORDER-101",
                null,
                null,
                null
        );

        when(chargeRepository.findByOrganizationIdAndEnvironmentAndReference(10L, ApiEnvironment.TEST, "REF-CHG-1"))
                .thenReturn(Optional.empty());

        PaymentProviderProfileQueryPort.ProviderProfile profile = new PaymentProviderProfileQueryPort.ProviderProfile(
                100L, 10L, ApiEnvironment.TEST, "PAYSTACK", null, null, Set.of("USSD_COLLECTION")
        );
        when(providerProfileQueryPort.findActiveProfile(10L, ApiEnvironment.TEST, "USSD_COLLECTION"))
                .thenReturn(Optional.of(profile));
        when(chargeRepository.nextIdentity()).thenReturn(111L);

        when(chargeProviderPort.initialize(any()))
                .thenThrow(new ChargeProviderException("Provider network timeout"));

        assertThrows(ChargeProviderException.class, () -> handler.execute(command));
        verify(chargeRepository, org.mockito.Mockito.times(2)).save(any());
    }
}
