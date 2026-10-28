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

        if (payoutGatewayPort == null) {
            payout.fail("No connected payout provider is configured");
            payoutRepository.save(payout);
            return null;
        }

        payout.markProcessing();
        payoutRepository.save(payout);

            try {
                String providerRef = payoutGatewayPort.initiateTransfer(
                        payout.getReference(),
                        payout.getAmount().amount(),
                        payout.getAmount().currency().name(),
                        payout.getRecipientBankCode(),
                        payout.getRecipientAccountNumber(),
                        payout.getNarration()
                );
                if (providerRef == null || providerRef.isBlank()) {
                    payout.fail("Provider did not return a transfer reference");
                } else {
                    // Initiation is not proof of settlement; confirmation arrives asynchronously.
                    payout.markPending(providerRef);
                }
                payoutRepository.save(payout);
            } catch (Exception e) {
                // A transport error can occur after the provider accepted the transfer.
                // Keep it unresolved until a signed webhook or server reconciliation confirms it.
                payoutRepository.save(payout);
            }

        return null;
    }
}
