package com.atlashub.pay.splits.domain.exceptions;

import com.atlashub.shared.domain.exception.ValidationException;

public class InvalidSplitPercentagesException extends ValidationException {
    public InvalidSplitPercentagesException() {
        super("Split percentages including platform fee must sum to exactly 100%");
    }

    public InvalidSplitPercentagesException(String message) {
        super(message);
    }
}
