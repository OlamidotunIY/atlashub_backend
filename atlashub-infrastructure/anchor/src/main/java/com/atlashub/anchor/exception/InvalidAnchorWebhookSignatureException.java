package com.atlashub.anchor.exception;

/** Raised when an inbound request cannot be authenticated as an Anchor webhook. */
public class InvalidAnchorWebhookSignatureException extends RuntimeException {
    public InvalidAnchorWebhookSignatureException() {
        super("Anchor webhook signature is invalid");
    }
}
