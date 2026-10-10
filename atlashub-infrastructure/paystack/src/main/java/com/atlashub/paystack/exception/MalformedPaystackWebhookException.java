package com.atlashub.paystack.exception;

public class MalformedPaystackWebhookException extends RuntimeException {
    public MalformedPaystackWebhookException(String message) { super(message); }
    public MalformedPaystackWebhookException(String message, Throwable cause) { super(message, cause); }
}
