package com.atlashub.charges.application.usecase;

import com.atlashub.charges.application.command.InitiateExternalChargeCommand;
import com.atlashub.charges.application.port.out.PaymentGatewayPort;
import com.atlashub.charges.domain.model.PaystackCharge;
import com.atlashub.charges.domain.repository.PaystackChargeRepository;
import com.atlashub.charges.domain.valueobject.ChargePurpose;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InitiateExternalChargeUseCase extends BaseUseCase<InitiateExternalChargeCommand, Void> {

    private final PaystackChargeRepository chargeRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final DomainEventPublisher publisher;

    public InitiateExternalChargeUseCase(
            PaystackChargeRepository chargeRepository,
            PaymentGatewayPort paymentGatewayPort,
            DomainEventPublisher publisher) {
        this.chargeRepository = chargeRepository;
        this.paymentGatewayPort = paymentGatewayPort;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(InitiateExternalChargeCommand command) {
        String reference = "INV-" + command.invoiceId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String metadata = String.valueOf(command.invoiceId());

        String checkoutUrl = paymentGatewayPort.initializeCharge(
                command.amount().amount(),
                command.amount().currency().name(),
                command.customerEmail(),
                reference,
                metadata,
                command.redirectUrl()
        );

        PaystackCharge charge = PaystackCharge.initiate(
                chargeRepository.nextIdentity(),
                command.invoiceId(),
                command.organizationId(),
                reference,
                checkoutUrl,
                command.amount(),
                ChargePurpose.PLATFORM_INVOICE
        );

        chargeRepository.save(charge);
        publishEvents(charge, publisher);

        return null;
    }
}