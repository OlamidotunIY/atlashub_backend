package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.util.Objects;

@Getter
public class LedgerAccountMapping extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final SourceSystem sourceSystem;
    private Long debitAccountId;
    private Long creditAccountId;
    private String description;

    public LedgerAccountMapping(Long id,
                                Long organizationId,
                                SourceSystem sourceSystem,
                                Long debitAccountId,
                                Long creditAccountId,
                                String description) {
        this.id = id;
        this.organizationId = organizationId;
        this.sourceSystem = sourceSystem;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.description = description;
    }

    public static LedgerAccountMapping create(Long id,
                                              Long organizationId,
                                              SourceSystem sourceSystem,
                                              Long debitAccountId,
                                              Long creditAccountId,
                                              String description) {
        Objects.requireNonNull(id, "LedgerAccountMapping ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(sourceSystem, "SourceSystem must not be null");
        Objects.requireNonNull(debitAccountId, "Debit account ID must not be null");
        Objects.requireNonNull(creditAccountId, "Credit account ID must not be null");

        return new LedgerAccountMapping(
                id,
                organizationId,
                sourceSystem,
                debitAccountId,
                creditAccountId,
                description
        );
    }

    public void updateMapping(Long debitAccountId, Long creditAccountId, String description) {
        Objects.requireNonNull(debitAccountId, "Debit account ID must not be null");
        Objects.requireNonNull(creditAccountId, "Credit account ID must not be null");
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.description = description;
    }

    @Override
    public Long getId() {
        return id;
    }
}
