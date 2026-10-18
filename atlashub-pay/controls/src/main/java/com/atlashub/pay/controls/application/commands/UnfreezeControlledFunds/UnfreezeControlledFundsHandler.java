package com.atlashub.pay.controls.application.commands.UnfreezeControlledFunds;

import com.atlashub.pay.controls.domain.exceptions.UnsupportedControlCapabilityException;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.shared.application.port.ControlledLedgerPort;
import com.atlashub.shared.application.port.ProviderPaymentControlPort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UnfreezeControlledFundsHandler extends Command<UnfreezeControlledFundsCommand, Void> {

    private final ControlledLedgerPort controlledLedgerPort;
    private final ProviderPaymentControlPort providerPaymentControlPort;

    public UnfreezeControlledFundsHandler(ControlledLedgerPort controlledLedgerPort,
                                          ProviderPaymentControlPort providerPaymentControlPort) {
        this.controlledLedgerPort = controlledLedgerPort;
        this.providerPaymentControlPort = providerPaymentControlPort;
    }

    @Override
    @Transactional
    public Void execute(UnfreezeControlledFundsCommand input) {
        PaymentRail rail = PaymentRail.valueOf(input.rail().toUpperCase());
        if (rail == PaymentRail.INTERNAL_LEDGER) {
            ControlledLedgerPort.LedgerControlResult result = controlledLedgerPort.releaseLedgerBalance(
                    input.organizationId(),
                    input.facilityId(),
                    input.amount(),
                    input.accountReference()
            );
            if (!result.successful()) {
                throw new UnsupportedControlCapabilityException("Failed to release internal ledger freeze: " + result.errorMessage());
            }
        } else if (rail == PaymentRail.VIRTUAL_ACCOUNT) {
            ProviderPaymentControlPort.ControlOperationResult result = providerPaymentControlPort.unfreezeFunds(
                    new ProviderPaymentControlPort.UnfreezeFundsRequest(
                            input.accountReference(),
                            input.facilityId(),
                            input.organizationId(),
                            input.providerControlReference(),
                            input.reason()
                    )
            );
            if (!result.successful()) {
                throw new UnsupportedControlCapabilityException("Failed to unfreeze virtual account: " + result.errorMessage());
            }
        }
        return null;
    }
}
