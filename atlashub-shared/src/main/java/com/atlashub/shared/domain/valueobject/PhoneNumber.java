package com.atlashub.shared.domain.valueobject;

import com.atlashub.shared.domain.exception.InvalidPhoneFormatException;

import com.atlashub.shared.domain.exception.ValidationException;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{6,14}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidPhoneFormatException("Phone number cannot be empty");
        }
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new InvalidPhoneFormatException("Invalid phone number format. Must be in E.164 format (e.g., +2348012345678) with 7-15 digits after the '+' sign.");
        }
    }
}



