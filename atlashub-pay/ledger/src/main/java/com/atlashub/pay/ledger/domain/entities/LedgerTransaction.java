package com.atlashub.pay.ledger.domain.entities;

import com.atlashub.shared.domain.entities.AggregateRoot;

public class LedgerTransaction extends AggregateRoot<Long> {
    private Long id;
    
    @Override
    public Long getId() {
        return id;
    }
}
