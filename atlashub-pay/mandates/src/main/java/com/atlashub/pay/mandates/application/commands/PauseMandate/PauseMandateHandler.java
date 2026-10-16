package com.atlashub.pay.mandates.application.commands.PauseMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PauseMandateHandler extends Command<PauseMandateCommand, Void> {

    private final PaymentMandateRepository mandateRepository;

    public PauseMandateHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional
    public Void execute(PauseMandateCommand input) {
        PaymentMandate mandate = mandateRepository.findByIdAndOrganizationId(input.mandateId(), input.organizationId())
                .orElseThrow(() -> new MandateNotFoundException("Payment mandate not found: " + input.mandateId()));

        mandate.pause();
        mandateRepository.save(mandate);
        return null;
    }
}
