package com.atlashub.shipping.domain.valueobject;

import com.atlashub.shared.domain.AggregateRoot;

public class Shipment extends AggregateRoot<Long> {
    private Long id;
    private String orderId;
    private ShipmentStatus status;
    
    public Shipment(String orderId) {
        this.orderId = orderId;
        this.status = ShipmentStatus.PENDING;
    }

    @Override
    public Long getId() {
        return id;
    }

    public enum ShipmentStatus {
        PENDING, SHIPPED, DELIVERED
    }
}
