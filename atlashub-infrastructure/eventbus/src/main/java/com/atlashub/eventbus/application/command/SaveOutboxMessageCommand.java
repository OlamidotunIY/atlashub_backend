package com.atlashub.eventbus.application.command;

import com.atlashub.shared.domain.event.EnvelopedDomainEvent;

public record SaveOutboxMessageCommand(String topic, EnvelopedDomainEvent<?> event) {
}
