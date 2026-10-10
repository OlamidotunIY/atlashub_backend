package com.atlashub.pay.charges.application.commands.ApplyRefundProviderStatus;

import com.atlashub.pay.charges.domain.exceptions.ChargeNotFoundException;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class ApplyRefundProviderStatusHandler extends Command<ApplyRefundProviderStatusCommand, Void> {
    private final ChargeRepository charges;

    public ApplyRefundProviderStatusHandler(ChargeRepository charges) {
        this.charges = charges;
    }

    @Override
    public Void execute(ApplyRefundProviderStatusCommand command) {
        var charge = charges.findByProviderReferenceAndEnvironment(
                        command.transactionReference(), command.environment())
                .orElseThrow(() -> new ChargeNotFoundException(command.transactionReference()));
        if ("processed".equalsIgnoreCase(command.status())) {
            charge.completeRefund(command.refundReference(), command.amount());
        } else if ("failed".equalsIgnoreCase(command.status())) {
            charge.failRefund(command.failureReason());
        } else if (command.refundReference() != null && !command.refundReference().isBlank()
                && charge.getProviderRefundReference() == null) {
            charge.markRefundSubmitted(command.refundReference());
        } else {
            return null;
        }
        charges.save(charge);
        return null;
    }
}
