package com.atlashub.shared.domain.valueobject;

import com.atlashub.shared.domain.exception.InvalidEmailFormatException;

import com.atlashub.shared.domain.exception.ValidationException;

import java.util.regex.Pattern;

public record EmailAddress(String value) {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailFormatException("Email address cannot be empty");
        }
        if (value.length() > 254) {
            throw new InvalidEmailFormatException("Email address cannot exceed 254 characters");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailFormatException("Invalid email address format");
        }
    }
}




