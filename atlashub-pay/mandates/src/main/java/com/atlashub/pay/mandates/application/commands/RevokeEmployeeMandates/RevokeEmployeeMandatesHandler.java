package com.atlashub.pay.mandates.application.commands.RevokeEmployeeMandates;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RevokeEmployeeMandatesHandler extends Command<RevokeEmployeeMandatesCommand, Void> {

    private final PaymentMandateRepository mandateRepository;

    public RevokeEmployeeMandatesHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional
    public Void execute(RevokeEmployeeMandatesCommand input) {
        String customerId = String.valueOf(input.userId());
        List<PaymentMandate> mandates = mandateRepository.findByOrganizationIdAndCustomerIdAndStatus(
                input.organizationId(), customerId, MandateStatus.ACTIVE
        );

        for (PaymentMandate mandate : mandates) {
            mandate.revoke();
            mandateRepository.save(mandate);
        }

        return null;
    }
}
