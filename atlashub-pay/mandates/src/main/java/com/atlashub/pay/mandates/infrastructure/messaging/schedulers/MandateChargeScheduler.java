package com.atlashub.pay.mandates.infrastructure.messaging.schedulers;

import com.atlashub.pay.mandates.application.commands.ChargeMandate.ChargeMandateCommand;
import com.atlashub.pay.mandates.application.commands.ChargeMandate.ChargeMandateHandler;
import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class MandateChargeScheduler {

    private static final Logger log = LoggerFactory.getLogger(MandateChargeScheduler.class);

    private final PaymentMandateRepository mandateRepository;
    private final ChargeMandateHandler chargeMandateHandler;

    public MandateChargeScheduler(PaymentMandateRepository mandateRepository, ChargeMandateHandler chargeMandateHandler) {
        this.mandateRepository = mandateRepository;
        this.chargeMandateHandler = chargeMandateHandler;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void chargeDueMandates() {
        log.info("Starting scheduled mandate charge execution");
        List<PaymentMandate> dueMandates = mandateRepository.findActiveMandatesDueForCharge(LocalDate.now());
        for (PaymentMandate mandate : dueMandates) {
            try {
                chargeMandateHandler.execute(new ChargeMandateCommand(mandate.getId()));
            } catch (Exception e) {
                log.error("Failed to process charge for mandate {}: {}", mandate.getId(), e.getMessage(), e);
            }
        }
        log.info("Completed scheduled mandate charge execution for {} mandates", dueMandates.size());
    }
}
