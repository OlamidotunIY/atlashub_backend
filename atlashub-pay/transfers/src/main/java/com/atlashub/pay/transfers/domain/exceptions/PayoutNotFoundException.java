package com.atlashub.pay.transfers.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class PayoutNotFoundException extends NotFoundException {

    public PayoutNotFoundException() {
        super("Payout not found");
    }

    public PayoutNotFoundException(String message) {
        super(message);
    }
}
