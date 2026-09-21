package com.atlashub.eventbus.domain.exception;

import com.atlashub.shared.domain.exception.NotFoundException;

public class EventNotFoundException extends NotFoundException {
    public EventNotFoundException(String message) { super(message); }
}
