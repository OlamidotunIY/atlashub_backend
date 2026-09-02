package com.atlashub.eventbus.adapter.in.messaging.listener;

import com.atlashub.eventbus.application.command.SaveOutboxMessageCommand;
import com.atlashub.eventbus.application.usecase.SaveOutboxMessageUseCase;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventListener {

    private final SaveOutboxMessageUseCase saveOutboxMessageUseCase;

    public SpringDomainEventListener(SaveOutboxMessageUseCase saveOutboxMessageUseCase) {
        this.saveOutboxMessageUseCase = saveOutboxMessageUseCase;
    }

    @EventListener
    public void handleDomainEvent(EnvelopedDomainEvent<?> envelopedEvent) {
        String topic = getTopicForEvent(envelopedEvent.event().getClass().getSimpleName());
        saveOutboxMessageUseCase.execute(new SaveOutboxMessageCommand(topic, envelopedEvent));
    }

    private String getTopicForEvent(String eventClassName) {
        if (eventClassName.startsWith("Organization") || eventClassName.startsWith("ApiKey")) {
            return "organization-events";
        } else if (eventClassName.startsWith("User")) {
            return "user-events";
        } else if (eventClassName.startsWith("Auth") || eventClassName.startsWith("Verification")) {
            return "auth-events";
        } else if (eventClassName.startsWith("Account") || eventClassName.startsWith("SplitRecipient")) {
            return "account-events";
        } else if (eventClassName.startsWith("Transaction") || eventClassName.startsWith("Transfer")) {
            return "transaction-events";
        } else if (eventClassName.startsWith("Charge")) {
            return "charge-events";
        } else if (eventClassName.startsWith("Ledger")) {
            return "ledger-events";
        } else if (eventClassName.startsWith("Settlement")) {
            return "settlement-events";
        } else if (eventClassName.startsWith("Admin")) {
            return "admin-events";
        }
        return "system-events";
    }
}
