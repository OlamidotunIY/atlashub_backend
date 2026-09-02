package com.atlashub.eventbus.application;

import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisherAdapter implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(EnvelopedDomainEvent<?> event) {
        applicationEventPublisher.publishEvent(event);
    }
}
