package com.atlashub.pay.mandates.application.commands.ChargeMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.shared.application.usecase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class ChargeMandateHandler extends Command<ChargeMandateCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(ChargeMandateHandler.class);

    private final PaymentMandateRepository mandateRepository;

    public ChargeMandateHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional
    public Void execute(ChargeMandateCommand input) {
        PaymentMandate mandate = mandateRepository.findById(input.mandateId())
                .orElseThrow(() -> new MandateNotFoundException("Payment mandate not found: " + input.mandateId()));

        if (mandate.getStatus() != MandateStatus.ACTIVE) {
            log.info("Skipping charge for mandate {} with status {}", mandate.getId(), mandate.getStatus());
            return null;
        }

        String chargeReference = "mandate-" + mandate.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        mandate.triggerCharge(chargeReference, ZonedDateTime.now());
        mandateRepository.save(mandate);

        return null;
    }
}
