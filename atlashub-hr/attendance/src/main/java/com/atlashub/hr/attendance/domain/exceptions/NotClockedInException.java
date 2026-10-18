package com.atlashub.hr.attendance.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class NotClockedInException extends BusinessRuleException {

    public NotClockedInException() {
        super("No open clock-in record found for today");
    }
}
