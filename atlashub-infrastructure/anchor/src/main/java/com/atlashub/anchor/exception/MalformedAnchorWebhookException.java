package com.atlashub.anchor.exception;

/** Raised when a verified Anchor webhook does not conform to Anchor's minimal event envelope. */
public class MalformedAnchorWebhookException extends RuntimeException {
    public MalformedAnchorWebhookException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedAnchorWebhookException(String message) {
        super(message);
    }
}
