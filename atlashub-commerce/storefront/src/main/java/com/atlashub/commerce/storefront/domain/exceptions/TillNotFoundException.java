package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class TillNotFoundException extends NotFoundException {

    public TillNotFoundException(Long id) {
        super("Till not found: " + id);
    }
}
