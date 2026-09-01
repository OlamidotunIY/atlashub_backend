package com.atlashub.storefront.domain.model;

import com.atlashub.shared.domain.AggregateRoot;

public class Store extends AggregateRoot<Long> {
    private Long id;
    private String name;
    private String organizationId;
    
    public Store(String name, String organizationId) {
        this.name = name;
        this.organizationId = organizationId;
    }

    @Override
    public Long getId() {
        return id;
    }
}
