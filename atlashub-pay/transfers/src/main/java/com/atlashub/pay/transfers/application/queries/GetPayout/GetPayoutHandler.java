package com.atlashub.pay.transfers.application.queries.GetPayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.exceptions.PayoutNotFoundException;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetPayoutHandler extends Query<GetPayoutQuery, PayoutResult> {

    private final PayoutRepository payoutRepository;

    public GetPayoutHandler(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PayoutResult execute(GetPayoutQuery query) {
        Payout payout = payoutRepository.findById(query.payoutId())
                .filter(p -> query.organizationId() == null || query.organizationId().equals(p.getOrganizationId()))
                .orElseThrow(PayoutNotFoundException::new);

        return toResult(payout);
    }

    public static PayoutResult toResult(Payout p) {
        return new PayoutResult(
                p.getId(),
                p.getOrganizationId(),
                p.getAmount() != null ? p.getAmount().amount() : null,
                p.getAmount() != null && p.getAmount().currency() != null ? p.getAmount().currency().name() : null,
                p.getRecipientBankCode(),
                p.getRecipientAccountNumber(),
                p.getRecipientAccountName(),
                p.getNarration(),
                p.getStatus() != null ? p.getStatus().name() : null,
                p.getReference(),
                p.getProvider() != null ? p.getProvider().name() : null,
                p.getProviderReference(),
                p.getSourceSystem() != null ? p.getSourceSystem().name() : null,
                p.getSourceReferenceId(),
                p.getInitiatedBy(),
                p.getApprovedBy(),
                p.getApprovedAt(),
                p.getFailureReason(),
                p.getCreatedAt(),
                p.getCompletedAt()
        );
    }
}
