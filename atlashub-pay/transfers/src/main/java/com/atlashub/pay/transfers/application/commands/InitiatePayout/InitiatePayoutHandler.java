package com.atlashub.pay.transfers.application.commands.InitiatePayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.exceptions.InvalidBankDetailsException;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.shared.application.port.PayoutGatewayPort;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class InitiatePayoutHandler extends Command<InitiatePayoutCommand, InitiatePayoutResponse> {

    private static final BigDecimal DEFAULT_APPROVAL_THRESHOLD = new BigDecimal("100000.00");

    private final PayoutRepository payoutRepository;
    private final PayoutGatewayPort payoutGatewayPort;

    public InitiatePayoutHandler(PayoutRepository payoutRepository,
                                 @Autowired(required = false) PayoutGatewayPort payoutGatewayPort) {
        this.payoutRepository = payoutRepository;
        this.payoutGatewayPort = payoutGatewayPort;
    }

    @Override
    @Transactional
    public InitiatePayoutResponse execute(InitiatePayoutCommand command) {
        Optional<Payout> existing = payoutRepository.findByReference(command.reference());
        if (existing.isPresent()) {
            Payout p = existing.get();
            return new InitiatePayoutResponse(
                    p.getId(),
                    p.getReference(),
                    p.getStatus().name(),
                    p.getStatus() == PayoutStatus.PENDING_APPROVAL
            );
        }

        String recipientName = command.recipientAccountName();
        if ((recipientName == null || recipientName.isBlank()) && payoutGatewayPort != null) {
            try {
                recipientName = payoutGatewayPort.resolveAccountName(command.recipientAccountNumber(), command.recipientBankCode());
            } catch (Exception e) {
                throw new InvalidBankDetailsException("Failed to resolve recipient account name: " + e.getMessage());
            }
        }
        if (recipientName == null || recipientName.isBlank()) {
            recipientName = "UNKNOWN RECIPIENT";
        }

        boolean requiresApproval = false;
        if (command.sourceSystem() != SourceSystem.PAYROLL && command.sourceSystem() != SourceSystem.COMMERCE_REFUND) {
            if (command.amount() != null && command.amount().compareTo(DEFAULT_APPROVAL_THRESHOLD) > 0) {
                requiresApproval = true;
            }
        }

        CurrencyCode currency = CurrencyCode.valueOf(command.currency() != null ? command.currency() : "NGN");
        Money money = Money.of(command.amount(), currency);

        Long payoutId = payoutRepository.nextIdentity();
        Payout payout = Payout.create(
                payoutId,
                command.organizationId(),
                money,
                command.recipientBankCode(),
                command.recipientAccountNumber(),
                recipientName,
                command.narration(),
                command.reference(),
                command.provider(),
                command.sourceSystem(),
                command.sourceReferenceId(),
                command.initiatedByUserId(),
                requiresApproval
        );

        Payout saved = payoutRepository.save(payout);

        return new InitiatePayoutResponse(
                saved.getId(),
                saved.getReference(),
                saved.getStatus().name(),
                requiresApproval
        );
    }
}
