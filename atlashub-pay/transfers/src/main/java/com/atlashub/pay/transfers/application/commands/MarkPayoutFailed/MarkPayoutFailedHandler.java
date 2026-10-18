package com.atlashub.pay.transfers.application.commands.MarkPayoutFailed;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.exceptions.PayoutNotFoundException;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MarkPayoutFailedHandler extends Command<MarkPayoutFailedCommand, Void> {

    private final PayoutRepository payoutRepository;

    public MarkPayoutFailedHandler(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    @Override
    @Transactional
    public Void execute(MarkPayoutFailedCommand command) {
        Payout payout = payoutRepository.findById(command.payoutId())
                .orElseThrow(PayoutNotFoundException::new);

        payout.fail(command.reason());
        payoutRepository.save(payout);

        return null;
    }
}
