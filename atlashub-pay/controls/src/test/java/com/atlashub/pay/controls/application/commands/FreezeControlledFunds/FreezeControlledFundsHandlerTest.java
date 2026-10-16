package com.atlashub.pay.controls.application.commands.FreezeControlledFunds;

import com.atlashub.pay.controls.domain.exceptions.UnsupportedControlCapabilityException;
import com.atlashub.shared.application.port.ControlledLedgerPort;
import com.atlashub.shared.application.port.ProviderPaymentControlPort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreezeControlledFundsHandlerTest {

    @Mock
    private ControlledLedgerPort controlledLedgerPort;

    @Mock
    private ProviderPaymentControlPort providerPaymentControlPort;

    private FreezeControlledFundsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FreezeControlledFundsHandler(controlledLedgerPort, providerPaymentControlPort);
    }

    @Test
    @DisplayName("Should successfully freeze internal ledger balance")
    void shouldFreezeInternalLedgerBalance() {
        when(controlledLedgerPort.freezeLedgerBalance(any(), any(), any(), any()))
                .thenReturn(ControlledLedgerPort.LedgerControlResult.success("FRZ-123"));

        FreezeControlledFundsCommand command = new FreezeControlledFundsCommand(
                100L,
                10L,
                "ACC-LEDGER-01",
                Money.of(new BigDecimal("50000"), CurrencyCode.NGN),
                "INTERNAL_LEDGER",
                "Policy breach"
        );

        handler.execute(command);

        verify(controlledLedgerPort).freezeLedgerBalance(10L, 100L, Money.of(new BigDecimal("50000"), CurrencyCode.NGN), "ACC-LEDGER-01");
    }

    @Test
    @DisplayName("Should reject freeze on external bank account")
    void shouldRejectFreezeOnExternalAccount() {
        FreezeControlledFundsCommand command = new FreezeControlledFundsCommand(
                100L,
                10L,
                "0123456789",
                Money.of(new BigDecimal("50000"), CurrencyCode.NGN),
                "EXTERNAL_ACCOUNT",
                "Cannot freeze external"
        );

        assertThrows(UnsupportedControlCapabilityException.class, () -> handler.execute(command));
    }
}
