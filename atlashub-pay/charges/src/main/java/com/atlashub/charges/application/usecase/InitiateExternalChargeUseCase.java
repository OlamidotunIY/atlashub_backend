package com.atlashub.charges.application.usecase;

import com.atlashub.charges.application.command.InitiateExternalChargeCommand;
import com.atlashub.charges.application.port.out.PaymentGatewayPort;
import com.atlashub.charges.domain.event.ExternalChargeInitiatedEvent;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class InitiateExternalChargeUseCase extends BaseUseCase<InitiateExternalChargeCommand, Void> {

    private final PaymentGatewayPort paymentGatewayPort;
    private final DomainEventPublisher publisher;

    public InitiateExternalChargeUseCase(PaymentGatewayPort paymentGatewayPort, DomainEventPublisher publisher) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.publisher = publisher;
    }

    @Override
    public Void execute(InitiateExternalChargeCommand command) {
        String reference = "INV-" + command.invoiceId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String metadata = String.valueOf(command.invoiceId());

        String checkoutUrl = paymentGatewayPort.initializeCharge(
                command.amount(),
                command.currency(),
                command.customerEmail(),
                reference,
                "PLATFORM_INVOICE",
                metadata,
                command.redirectUrl()
        );

        ExternalChargeInitiatedEvent event = new ExternalChargeInitiatedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(command.invoiceId()),
                ZonedDateTime.now(),
                new ExternalChargeInitiatedEvent.Payload(
                        command.invoiceId(),
                        checkoutUrl,
                        reference
                )
        );
        publisher.publish(EnvelopedDomainEvent.wrap(event));

        return null;
    }
}