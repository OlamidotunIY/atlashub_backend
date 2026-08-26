package com.atlashub.catalog.domain.model;

import com.atlashub.shared.domain.AggregateRoot;

public class HubProduct extends AggregateRoot<Long> {
    private Long id;
    private String name;
    private String description;
    
    public HubProduct(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public Long getId() {
        return id;
    }
}
