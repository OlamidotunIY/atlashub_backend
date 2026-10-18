package com.atlashub.pay.mandates.application.commands.ExpireMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExpireMandateHandler extends Command<ExpireMandateCommand, Void> {

    private final PaymentMandateRepository mandateRepository;

    public ExpireMandateHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional
    public Void execute(ExpireMandateCommand input) {
        PaymentMandate mandate = mandateRepository.findById(input.mandateId())
                .orElseThrow(() -> new MandateNotFoundException("Payment mandate not found: " + input.mandateId()));

        mandate.markExpired(input.reason());
        mandateRepository.save(mandate);
        return null;
    }
}
