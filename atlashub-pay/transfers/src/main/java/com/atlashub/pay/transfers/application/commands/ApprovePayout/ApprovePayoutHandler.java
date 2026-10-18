package com.atlashub.pay.transfers.application.commands.ApprovePayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.exceptions.PayoutNotFoundException;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApprovePayoutHandler extends Command<ApprovePayoutCommand, Void> {

    private final PayoutRepository payoutRepository;

    public ApprovePayoutHandler(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    @Override
    @Transactional
    public Void execute(ApprovePayoutCommand command) {
        Payout payout = payoutRepository.findByIdWithPessimisticLock(command.payoutId())
                .orElseThrow(PayoutNotFoundException::new);

        payout.approve(command.approverUserId());
        payoutRepository.save(payout);

        return null;
    }
}
