package com.atlashub.pay.mandates.application.commands.AdvanceMandateDate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdvanceMandateDateHandler extends Command<AdvanceMandateDateCommand, Void> {

    private final PaymentMandateRepository mandateRepository;

    public AdvanceMandateDateHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional
    public Void execute(AdvanceMandateDateCommand input) {
        PaymentMandate mandate = mandateRepository.findById(input.mandateId())
                .orElseThrow(() -> new MandateNotFoundException("Payment mandate not found: " + input.mandateId()));

        mandate.recordChargeSuccess(input.chargeReference());
        mandateRepository.save(mandate);
        return null;
    }
}
