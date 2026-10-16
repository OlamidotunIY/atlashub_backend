package com.atlashub.pay.mandates.application.commands.RevokeMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RevokeMandateHandler extends Command<RevokeMandateCommand, Void> {

    private final PaymentMandateRepository mandateRepository;

    public RevokeMandateHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional
    public Void execute(RevokeMandateCommand input) {
        PaymentMandate mandate;
        if (input.organizationId() != null) {
            mandate = mandateRepository.findByIdAndOrganizationId(input.mandateId(), input.organizationId())
                    .orElseThrow(() -> new MandateNotFoundException("Payment mandate not found: " + input.mandateId()));
        } else {
            mandate = mandateRepository.findById(input.mandateId())
                    .orElseThrow(() -> new MandateNotFoundException("Payment mandate not found: " + input.mandateId()));
        }

        mandate.revoke();
        mandateRepository.save(mandate);
        return null;
    }
}
