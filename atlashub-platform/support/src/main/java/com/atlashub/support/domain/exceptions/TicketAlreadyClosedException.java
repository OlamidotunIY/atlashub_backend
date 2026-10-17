package com.atlashub.support.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class TicketAlreadyClosedException extends BusinessRuleException {
    public TicketAlreadyClosedException(Long ticketId) {
        super("Ticket " + ticketId + " is already closed");
    }
}
