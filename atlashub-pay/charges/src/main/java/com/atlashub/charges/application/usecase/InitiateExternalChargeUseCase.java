package com.atlashub.charges.application.usecase;

import com.atlashub.charges.application.command.InitiateExternalChargeCommand;
import com.atlashub.charges.application.port.out.PaymentGatewayPort;
import com.atlashub.charges.application.port.out.PaymentGatewayRouterPort;
import com.atlashub.charges.domain.model.ExternalCharge;
import com.atlashub.charges.domain.repository.ExternalChargeRepository;
import com.atlashub.charges.domain.valueobject.ChargePurpose;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InitiateExternalChargeUseCase extends BaseUseCase<InitiateExternalChargeCommand, Void> {

    private final ExternalChargeRepository chargeRepository;
    private final PaymentGatewayRouterPort paymentGatewayRouterPort;
    private final DomainEventPublisher publisher;

    public InitiateExternalChargeUseCase(
            ExternalChargeRepository chargeRepository,
            PaymentGatewayRouterPort paymentGatewayRouterPort,
            DomainEventPublisher publisher) {
        this.chargeRepository = chargeRepository;
        this.paymentGatewayRouterPort = paymentGatewayRouterPort;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(InitiateExternalChargeCommand command) {
        String reference = "INV-" + command.purposeId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String metadata = String.valueOf(command.purposeId());

        PaymentGatewayPort paymentGatewayPort = paymentGatewayRouterPort.resolve(command.organizationId());

        String checkoutUrl = paymentGatewayPort.initializeCharge(
                command.amount().amount(),
                command.amount().currency().name(),
                command.customerEmail(),
                reference,
                metadata,
                command.redirectUrl()
        );

        ExternalCharge charge = ExternalCharge.initiate(
                chargeRepository.nextIdentity(),
                command.purposeId(),
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
