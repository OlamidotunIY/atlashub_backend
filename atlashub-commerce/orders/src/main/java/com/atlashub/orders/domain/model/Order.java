package com.atlashub.orders.domain.model;

import com.atlashub.shared.domain.AggregateRoot;

public class Order extends AggregateRoot<Long> {
    private Long id;
    private String customerId;
    
    public Order(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public Long getId() {
        return id;
    }
}
