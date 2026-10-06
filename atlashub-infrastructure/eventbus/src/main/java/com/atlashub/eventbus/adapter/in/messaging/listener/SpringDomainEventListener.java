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
        String topic = getTopicForEvent(envelopedEvent.event().getClass());
        saveOutboxMessageUseCase.execute(new SaveOutboxMessageCommand(topic, envelopedEvent));
    }

    private String getTopicForEvent(Class<?> eventType) {
        String eventClassName = eventType.getSimpleName();
        if (eventType.getPackageName().startsWith("com.atlashub.compliance.")) {
            return "compliance-events";
        }
        if (eventType.getPackageName().startsWith("com.atlashub.pay.")
                && eventClassName.startsWith("ProviderOnboarding")) {
            return "pay-events";
        }
        if (eventClassName.startsWith("ApiKey") || eventClassName.startsWith("CustomRole")
                || eventClassName.startsWith("Invitation") || eventClassName.startsWith("Member")) {
            return "iam-events";
        } else if (eventClassName.startsWith("Organization")) {
            return "organization-events";
        } else if (eventClassName.startsWith("User")) {
            return "user-events";
        } else if (eventClassName.startsWith("Auth") || eventClassName.startsWith("Otp")) {
            return "auth-events";
        }
        return "system-events";
    }
}
