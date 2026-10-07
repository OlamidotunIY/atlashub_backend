package com.atlashub.notifications.domain.exceptions;

import com.atlashub.shared.domain.exception.DomainException;

public class OtpDeliveryException extends DomainException {
    public OtpDeliveryException(String message, Throwable cause) { super(message, cause); }
    public OtpDeliveryException(String message) { super(message); }
}
