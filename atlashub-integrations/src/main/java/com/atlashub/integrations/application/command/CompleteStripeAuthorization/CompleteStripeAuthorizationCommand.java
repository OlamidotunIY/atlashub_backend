package com.atlashub.integrations.application.command.CompleteStripeAuthorization;

public record CompleteStripeAuthorizationCommand(String state, String code, String error) {}
