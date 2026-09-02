package com.atlashub.catalog.application.command;

public record UpdateHubProductCommand(Long productId, String name, String description) {

}
