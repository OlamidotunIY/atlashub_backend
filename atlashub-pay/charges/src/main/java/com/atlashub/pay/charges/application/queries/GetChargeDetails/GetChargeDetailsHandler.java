package com.atlashub.pay.charges.application.queries.GetChargeDetails;

import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.exceptions.ChargeNotFoundException;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

@Component
public class GetChargeDetailsHandler extends Query<GetChargeDetailsQuery, ChargeResult> {

    private final ChargeRepository chargeRepository;

    public GetChargeDetailsHandler(ChargeRepository chargeRepository) {
        this.chargeRepository = chargeRepository;
    }

    @Override
    public ChargeResult execute(GetChargeDetailsQuery query) {
        Charge charge = chargeRepository.findById(query.chargeId())
                .orElseThrow(() -> new ChargeNotFoundException(String.valueOf(query.chargeId())));

        if (!charge.getOrganizationId().equals(query.organizationId()) ||
                charge.getEnvironment() != query.environment()) {
            throw new ChargeNotFoundException(String.valueOf(query.chargeId()));
        }

        return new ChargeResult(charge.getId(), charge.getOrganizationId(), charge.getEnvironment(),
                charge.getReference(), charge.getAmount(), charge.getChannel(), charge.getProvider(),
                charge.getProviderProfileId(), charge.getProviderReference(), charge.getSourceSystem(),
                charge.getSourceReferenceId(), charge.getCustomerReferenceId(), charge.getProviderFee(),
                charge.getStatus(), charge.getAuthorizationUrl(), charge.getAccessCode(),
                charge.getFailureMessage(), charge.getProviderRefundReference(), charge.getRefundReason(),
                charge.getRefundedAt(), charge.getDisputeReference(), charge.getDisputeStatus(),
                charge.getDisputeReason(), charge.getSuccessfulAt(), charge.getExpiresAt(), charge.getCreatedAt(),
                charge.getUpdatedAt());
    }
}
