package com.atlashub.authentication.domain.services;

import com.atlashub.authentication.domain.exceptions.AuthenticationInvariantException;

public final class PasswordPolicy {
    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new AuthenticationInvariantException("Password must be at least 8 characters long");
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean special = false;
        for (char character : password.toCharArray()) {
            upper |= Character.isUpperCase(character);
            lower |= Character.isLowerCase(character);
            digit |= Character.isDigit(character);
            special |= !Character.isLetterOrDigit(character) && !Character.isWhitespace(character);
        }
        if (!upper || !lower || !digit || !special) {
            throw new AuthenticationInvariantException(
                    "Password must contain uppercase, lowercase, number, and special characters");
        }
    }
}
