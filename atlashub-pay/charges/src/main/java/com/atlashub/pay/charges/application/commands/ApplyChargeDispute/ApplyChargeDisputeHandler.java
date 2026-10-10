package com.atlashub.pay.charges.application.commands.ApplyChargeDispute;

import com.atlashub.pay.charges.domain.exceptions.ChargeNotFoundException;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class ApplyChargeDisputeHandler extends Command<ApplyChargeDisputeCommand,Void>{
    private final ChargeRepository charges;
    public ApplyChargeDisputeHandler(ChargeRepository charges){this.charges=charges;}
    @Override public Void execute(ApplyChargeDisputeCommand command){
        var charge=charges.findByProviderReferenceAndEnvironment(command.transactionReference(),command.environment())
                .orElseThrow(()->new ChargeNotFoundException(command.transactionReference()));
        if("charge.dispute.resolve".equalsIgnoreCase(command.eventType()))
            charge.resolveDispute(command.disputeReference(),command.reason());
        else charge.recordDispute(command.disputeReference(),command.reason());
        charges.save(charge);return null;
    }
}
