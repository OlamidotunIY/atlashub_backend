package com.atlashub.pay.charges.application.commands;

import com.atlashub.pay.charges.application.commands.ReconcilePendingCharges.ReconcilePendingChargesCommand;
import com.atlashub.pay.charges.application.commands.ReconcilePendingCharges.ReconcilePendingChargesHandler;
import com.atlashub.pay.charges.application.commands.ReconcilePendingCharges.ReconcilePendingChargesResult;
import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
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
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconcilePendingChargesHandlerTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private ChargeProviderPort chargeProviderPort;

    @InjectMocks
    private ReconcilePendingChargesHandler handler;

    private Charge createPendingCharge(String providerRef) {
        Charge charge = Charge.initialize(
                1L,
                10L,
                ApiEnvironment.TEST,
                "REF-100",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD,
                20L,
                "COMMERCE",
                "ORD-999"
        );
        charge.markPending(providerRef, "https://checkout.paystack.com/123", "AUTH_CODE_123");
        return charge;
    }

    @Test
    @DisplayName("Should successfully reconcile pending charge when provider returns success")
    void shouldReconcileSuccessfulPendingCharge() {
        Charge charge = createPendingCharge("PAYSTACK-REF-1");
        ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(15);
        when(chargeRepository.findPendingCharges(cutoff)).thenReturn(List.of(charge));
        when(chargeProviderPort.fetchStatus("PAYSTACK-REF-1", ApiEnvironment.TEST)).thenReturn(
                new ChargeProviderPort.ProviderStatus(
                        "PAYSTACK-REF-1",
                        "success",
                        new BigDecimal("5000.00"),
                        CurrencyCode.NGN,
                        "card"
                )
        );

        ReconcilePendingChargesResult result = handler.execute(new ReconcilePendingChargesCommand(cutoff));

        assertEquals(1, result.reconciledCount());
        assertEquals(0, result.failureCount());
        assertEquals(ChargeStatus.SUCCESSFUL, charge.getStatus());
        verify(chargeRepository).save(charge);
    }

    @Test
    @DisplayName("Should reconcile pending charge to FAILED when provider returns failed or abandoned")
    void shouldReconcileFailedOrAbandonedCharge() {
        Charge charge = createPendingCharge("PAYSTACK-REF-2");
        ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(15);
        when(chargeRepository.findPendingCharges(cutoff)).thenReturn(List.of(charge));
        when(chargeProviderPort.fetchStatus("PAYSTACK-REF-2", ApiEnvironment.TEST)).thenReturn(
                new ChargeProviderPort.ProviderStatus(
                        "PAYSTACK-REF-2",
                        "abandoned",
                        new BigDecimal("5000.00"),
                        CurrencyCode.NGN,
                        "card"
                )
        );

        ReconcilePendingChargesResult result = handler.execute(new ReconcilePendingChargesCommand(cutoff));

        assertEquals(1, result.reconciledCount());
        assertEquals(0, result.failureCount());
        assertEquals(ChargeStatus.FAILED, charge.getStatus());
        verify(chargeRepository).save(charge);
    }

    @Test
    @DisplayName("Should skip charge when provider reference is null or blank")
    void shouldSkipChargeWhenProviderReferenceIsBlank() {
        Charge charge = Charge.initialize(
                1L,
                10L,
                ApiEnvironment.TEST,
                "REF-100",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD,
                20L,
                "COMMERCE",
                "ORD-999"
        );
        ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(15);
        when(chargeRepository.findPendingCharges(cutoff)).thenReturn(List.of(charge));

        ReconcilePendingChargesResult result = handler.execute(new ReconcilePendingChargesCommand(cutoff));

        assertEquals(0, result.reconciledCount());
        assertEquals(0, result.failureCount());
        verify(chargeProviderPort, never()).fetchStatus(any(), any());
        verify(chargeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should increment failureCount when provider status fetch throws exception")
    void shouldHandleProviderExceptionAndRecordFailure() {
        Charge charge = createPendingCharge("PAYSTACK-REF-3");
        ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(15);
        when(chargeRepository.findPendingCharges(cutoff)).thenReturn(List.of(charge));
        when(chargeProviderPort.fetchStatus("PAYSTACK-REF-3", ApiEnvironment.TEST))
                .thenThrow(new RuntimeException("Network timeout"));

        ReconcilePendingChargesResult result = handler.execute(new ReconcilePendingChargesCommand(cutoff));

        assertEquals(0, result.reconciledCount());
        assertEquals(1, result.failureCount());
        assertEquals(ChargeStatus.PENDING, charge.getStatus());
        verify(chargeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should leave charge pending when provider status is ongoing/pending")
    void shouldLeavePendingWhenStatusIsStillPending() {
        Charge charge = createPendingCharge("PAYSTACK-REF-4");
        ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(15);
        when(chargeRepository.findPendingCharges(cutoff)).thenReturn(List.of(charge));
        when(chargeProviderPort.fetchStatus("PAYSTACK-REF-4", ApiEnvironment.TEST)).thenReturn(
                new ChargeProviderPort.ProviderStatus(
                        "PAYSTACK-REF-4",
                        "pending",
                        new BigDecimal("5000.00"),
                        CurrencyCode.NGN,
                        "card"
                )
        );

        ReconcilePendingChargesResult result = handler.execute(new ReconcilePendingChargesCommand(cutoff));

        assertEquals(0, result.reconciledCount());
        assertEquals(0, result.failureCount());
        assertEquals(ChargeStatus.PENDING, charge.getStatus());
        verify(chargeRepository, never()).save(any());
    }
}
