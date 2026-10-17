package com.atlashub.analytics.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class AnalyticsInvariantException extends BusinessRuleException {
    public AnalyticsInvariantException(String message) {
        super(message);
    }
}
