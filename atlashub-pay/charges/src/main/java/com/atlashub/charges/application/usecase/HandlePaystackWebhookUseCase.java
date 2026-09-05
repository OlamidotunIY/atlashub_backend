package com.atlashub.charges.application.usecase;

import com.atlashub.charges.application.command.HandlePaystackWebhookCommand;
import com.atlashub.charges.domain.model.ExternalCharge;
import com.atlashub.charges.domain.repository.ExternalChargeRepository;
import com.atlashub.charges.domain.valueobject.PaystackEventType;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HandlePaystackWebhookUseCase extends BaseUseCase<HandlePaystackWebhookCommand, Void> {

    private final ExternalChargeRepository chargeRepository;
    private final DomainEventPublisher publisher;

    public HandlePaystackWebhookUseCase(ExternalChargeRepository chargeRepository, DomainEventPublisher publisher) {
        this.chargeRepository = chargeRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(HandlePaystackWebhookCommand command) {
        PaystackEventType type;
        try {
            type = PaystackEventType.from(command.event());
        } catch (Exception e) {
            return null; // Ignore unknown events
        }

        chargeRepository.findByReference(command.reference()).ifPresent(charge -> {
            if (type == PaystackEventType.CHARGE_SUCCESS) {
                charge.markSuccessful();
            } else if (type == PaystackEventType.CHARGE_FAILURE) {
                charge.markFailed("Webhook reported failure");
            }
            chargeRepository.save(charge);
            publishEvents(charge, publisher);
        });

        return null;
    }
}
