package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class KitchenOrderTicketNotFoundException extends NotFoundException {

    public KitchenOrderTicketNotFoundException(Long id) {
        super("Kitchen order ticket not found: " + id);
    }
}
