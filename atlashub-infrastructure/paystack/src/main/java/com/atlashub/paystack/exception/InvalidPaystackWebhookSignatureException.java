package com.atlashub.paystack.exception;

public class InvalidPaystackWebhookSignatureException extends RuntimeException {
    public InvalidPaystackWebhookSignatureException() { super("Paystack webhook signature is invalid"); }
}
