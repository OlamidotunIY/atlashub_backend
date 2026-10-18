package com.atlashub.pay.controls.application.commands.FreezeControlledFunds;

import com.atlashub.pay.controls.domain.exceptions.UnsupportedControlCapabilityException;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.shared.application.port.ControlledLedgerPort;
import com.atlashub.shared.application.port.ProviderPaymentControlPort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FreezeControlledFundsHandler extends Command<FreezeControlledFundsCommand, Void> {

    private final ControlledLedgerPort controlledLedgerPort;
    private final ProviderPaymentControlPort providerPaymentControlPort;

    public FreezeControlledFundsHandler(ControlledLedgerPort controlledLedgerPort,
                                        ProviderPaymentControlPort providerPaymentControlPort) {
        this.controlledLedgerPort = controlledLedgerPort;
        this.providerPaymentControlPort = providerPaymentControlPort;
    }

    @Override
    @Transactional
    public Void execute(FreezeControlledFundsCommand input) {
        PaymentRail rail = PaymentRail.valueOf(input.rail().toUpperCase());
        if (rail == PaymentRail.EXTERNAL_ACCOUNT) {
            throw new UnsupportedControlCapabilityException("External bank accounts outside AtlasHub control cannot be frozen.");
        }

        if (rail == PaymentRail.INTERNAL_LEDGER) {
            ControlledLedgerPort.LedgerControlResult result = controlledLedgerPort.freezeLedgerBalance(
                    input.organizationId(),
                    input.facilityId(),
                    input.amount(),
                    input.accountReference()
            );
            if (!result.successful()) {
                throw new UnsupportedControlCapabilityException("Failed to freeze internal ledger: " + result.errorMessage());
            }
        } else if (rail == PaymentRail.VIRTUAL_ACCOUNT) {
            ProviderPaymentControlPort.ControlOperationResult result = providerPaymentControlPort.freezeFunds(
                    new ProviderPaymentControlPort.FreezeFundsRequest(
                            input.accountReference(),
                            input.facilityId(),
                            input.organizationId(),
                            rail.name(),
                            input.reason()
                    )
            );
            if (!result.successful()) {
                throw new UnsupportedControlCapabilityException("Failed to freeze virtual account: " + result.errorMessage());
            }
        }
        return null;
    }
}
