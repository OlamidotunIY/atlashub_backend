package com.atlashub.anchor.exception;

/** Raised when Anchor already has a webhook label with a different immutable callback contract. */
public class AnchorWebhookSubscriptionConflictException extends RuntimeException {
    public AnchorWebhookSubscriptionConflictException(String message) {
        super(message);
    }
}
