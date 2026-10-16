package com.atlashub.pay.mandates.application.queries.GetMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetMandateHandler extends Query<GetMandateQuery, MandateResult> {

    private final PaymentMandateRepository mandateRepository;

    public GetMandateHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MandateResult execute(GetMandateQuery query) {
        PaymentMandate mandate = mandateRepository.findByIdAndOrganizationId(query.mandateId(), query.organizationId())
                .orElseThrow(() -> new MandateNotFoundException("Payment mandate not found: " + query.mandateId()));

        return toResult(mandate);
    }

    public static MandateResult toResult(PaymentMandate m) {
        return new MandateResult(
                m.getId(),
                m.getOrganizationId(),
                m.getCustomerId(),
                m.getEmail() != null ? m.getEmail().value() : null,
                m.getAmount() != null ? m.getAmount().amount() : null,
                m.getAmount() != null && m.getAmount().currency() != null ? m.getAmount().currency().name() : "NGN",
                m.getFrequency() != null ? m.getFrequency().name() : null,
                m.getStatus() != null ? m.getStatus().name() : null,
                m.getNextChargeDate(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}
