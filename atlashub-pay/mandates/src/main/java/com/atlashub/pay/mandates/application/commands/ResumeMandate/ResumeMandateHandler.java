package com.atlashub.pay.mandates.application.commands.ResumeMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ResumeMandateHandler extends Command<ResumeMandateCommand, Void> {

    private final PaymentMandateRepository mandateRepository;

    public ResumeMandateHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional
    public Void execute(ResumeMandateCommand input) {
        PaymentMandate mandate = mandateRepository.findByIdAndOrganizationId(input.mandateId(), input.organizationId())
                .orElseThrow(() -> new MandateNotFoundException("Payment mandate not found: " + input.mandateId()));

        mandate.resume();
        mandateRepository.save(mandate);
        return null;
    }
}
