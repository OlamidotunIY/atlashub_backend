package com.atlashub.pay.settlement.domain.entities;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class SettlementPollCursor extends AggregateRoot<Long> {
    private final Long id;
    private final ApiEnvironment environment;
    private final String provider;
    private final String subaccountCode;
    private String lastProviderSettlementId;
    private ZonedDateTime updatedAt;

    public SettlementPollCursor(Long id, ApiEnvironment environment, String provider, String subaccountCode,
                                String lastProviderSettlementId, ZonedDateTime updatedAt) {
        this.id = id;
        this.environment = environment;
        this.provider = provider;
        this.subaccountCode = subaccountCode;
        this.lastProviderSettlementId = lastProviderSettlementId;
        this.updatedAt = updatedAt;
        if (id == null || environment == null || provider == null || provider.isBlank() || subaccountCode == null ||
                subaccountCode.isBlank() || updatedAt == null)
            throw new IllegalArgumentException("Valid settlement cursor is required");
    }

    public static SettlementPollCursor start(Long id, ApiEnvironment env, String provider, String code) {
        return new SettlementPollCursor(id, env, provider, code, null, ZonedDateTime.now());
    }

    public void advance(String settlementId) {
        if (settlementId == null || settlementId.isBlank())
            throw new IllegalArgumentException("Settlement id is required");
        lastProviderSettlementId = settlementId;
        updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
