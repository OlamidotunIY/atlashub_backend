package com.atlashub.support.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class TicketNotFoundException extends NotFoundException {
    public TicketNotFoundException(Long ticketId) {
        super("Support ticket not found: " + ticketId);
    }
}
