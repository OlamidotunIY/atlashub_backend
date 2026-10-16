package com.atlashub.pay.transfers.application.commands.InitiateBulkPayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class InitiateBulkPayoutHandler extends Command<InitiateBulkPayoutCommand, InitiateBulkPayoutResponse> {

    private final PayoutRepository payoutRepository;

    public InitiateBulkPayoutHandler(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    @Override
    @Transactional
    public InitiateBulkPayoutResponse execute(InitiateBulkPayoutCommand command) {
        List<Long> payoutIds = new ArrayList<>();

        if (command.items() != null) {
            for (InitiateBulkPayoutCommand.BulkPayoutItem item : command.items()) {
                Long payoutId = payoutRepository.nextIdentity();
                CurrencyCode currency = CurrencyCode.valueOf(item.currency() != null ? item.currency() : "NGN");
                Money money = Money.of(item.amount(), currency);

                Payout payout = Payout.create(
                        payoutId,
                        command.organizationId(),
                        money,
                        item.recipientBankCode(),
                        item.recipientAccountNumber(),
                        item.recipientAccountName() != null ? item.recipientAccountName() : "UNKNOWN",
                        item.narration(),
                        item.itemReference(),
                        PaymentProvider.PAYSTACK,
                        command.sourceSystem(),
                        command.sourceReferenceId() != null ? command.sourceReferenceId() : command.bulkReference(),
                        command.initiatedByUserId(),
                        true
                );

                payoutRepository.save(payout);
                payoutIds.add(payoutId);
            }
        }

        return new InitiateBulkPayoutResponse(
                command.bulkReference(),
                payoutIds.size(),
                payoutIds
        );
    }
}
