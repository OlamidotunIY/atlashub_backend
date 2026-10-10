package com.atlashub.accounting.gl.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class JournalEntryNotFoundException extends NotFoundException {

    public JournalEntryNotFoundException(Long id) {
        super("Journal entry not found: " + id);
    }
}
