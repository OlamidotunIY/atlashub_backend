package com.atlashub.eventbus.application.command;

public record ProcessOutboxMessagesCommand(int batchSize) {
}
