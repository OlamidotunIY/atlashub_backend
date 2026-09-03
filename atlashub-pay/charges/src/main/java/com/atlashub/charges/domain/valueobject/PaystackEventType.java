package com.atlashub.charges.domain.valueobject;

import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.charges.domain.exception.ChargesErrorCode;

public enum PaystackEventType {
    CHARGE_SUCCESS("charge.success"),
    CHARGE_FAILURE("charge.failure");

    private final String value;

    PaystackEventType(String value) { this.value = value; }

    public String value() { return value; }

    public static PaystackEventType from(String raw) {
        for (PaystackEventType t : values()) {
            if (t.value.equals(raw)) return t;
        }
        throw new BusinessRuleException(ChargesErrorCode.UNKNOWN_EVENT_TYPE, "Unknown Paystack event: " + raw);
    }
}
