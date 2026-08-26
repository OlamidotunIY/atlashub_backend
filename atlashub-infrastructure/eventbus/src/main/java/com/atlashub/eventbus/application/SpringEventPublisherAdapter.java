package com.atlashub.eventbus.application;

import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.event.EnvelopedDomainEvent;
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
