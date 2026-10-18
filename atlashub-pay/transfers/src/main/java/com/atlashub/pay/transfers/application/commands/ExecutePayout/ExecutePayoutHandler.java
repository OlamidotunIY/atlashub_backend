package com.atlashub.pay.transfers.application.commands.ExecutePayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.exceptions.PayoutNotFoundException;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.shared.application.port.PayoutGatewayPort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExecutePayoutHandler extends Command<ExecutePayoutCommand, Void> {

    private final PayoutRepository payoutRepository;
    private final PayoutGatewayPort payoutGatewayPort;

    public ExecutePayoutHandler(PayoutRepository payoutRepository,
                                @Autowired(required = false) PayoutGatewayPort payoutGatewayPort) {
        this.payoutRepository = payoutRepository;
        this.payoutGatewayPort = payoutGatewayPort;
    }

    @Override
    @Transactional
    public Void execute(ExecutePayoutCommand command) {
        Payout payout = payoutRepository.findById(command.payoutId())
                .orElseThrow(PayoutNotFoundException::new);

        payout.markProcessing();
        payoutRepository.save(payout);

        if (payoutGatewayPort != null) {
            try {
                String providerRef = payoutGatewayPort.initiateTransfer(
                        payout.getReference(),
                        payout.getAmount().amount(),
                        payout.getAmount().currency().name(),
                        payout.getRecipientBankCode(),
                        payout.getRecipientAccountNumber(),
                        payout.getNarration()
                );
                payout.complete(providerRef != null ? providerRef : ("TRF-" + payout.getId()));
                payoutRepository.save(payout);
            } catch (Exception e) {
                payout.fail(e.getMessage());
                payoutRepository.save(payout);
            }
        } else {
            payout.complete("MOCK-PROVIDER-REF-" + payout.getId());
            payoutRepository.save(payout);
        }

        return null;
    }
}
