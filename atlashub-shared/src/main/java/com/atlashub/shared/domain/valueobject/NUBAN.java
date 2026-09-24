package com.atlashub.shared.domain.valueobject;

import com.atlashub.shared.domain.exception.InvalidNubanFormatException;

import com.atlashub.shared.domain.exception.MissingRequiredFieldException;

import com.atlashub.shared.domain.exception.ValidationException;

public record NUBAN(String value) {
    public NUBAN {
        if (value == null || value.isBlank()) {
            throw new MissingRequiredFieldException("NUBAN cannot be null or blank");
        }
        if (!value.matches("^\\d{10}$")) {
            throw new InvalidNubanFormatException("NUBAN must be exactly 10 digits");
        }
    }
}



