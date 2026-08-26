package com.atlashub.eventbus.application.command;

import com.atlashub.shared.event.EnvelopedDomainEvent;

public record SaveOutboxMessageCommand(String topic, EnvelopedDomainEvent<?> event) {
}
