package com.atlashub.pay.settlement.domain.entities;

import com.atlashub.pay.settlement.domain.exceptions.InvalidSettlementStateException;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class SettlementCreditEvidence extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private final Long anchorDepositAccountId;
    private final String anchorTransferReference;
    private final Money receivedAmount;
    private final ZonedDateTime receivedAt;
    private Long matchedSettlementId;
    private final ZonedDateTime createdAt;

    public SettlementCreditEvidence(Long id, Long organizationId, ApiEnvironment environment,
                                    Long anchorDepositAccountId, String anchorTransferReference, Money receivedAmount,
                                    ZonedDateTime receivedAt, Long matchedSettlementId, ZonedDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.anchorDepositAccountId = anchorDepositAccountId;
        this.anchorTransferReference = anchorTransferReference;
        this.receivedAmount = receivedAmount;
        this.receivedAt = receivedAt;
        this.matchedSettlementId = matchedSettlementId;
        this.createdAt = createdAt;
        if (id == null || organizationId == null || environment == null || anchorDepositAccountId == null ||
                anchorTransferReference == null || anchorTransferReference.isBlank() || receivedAmount == null ||
                receivedAmount.amount().signum() <= 0 || receivedAt == null || createdAt == null) {
            throw new InvalidSettlementStateException("Complete Anchor credit evidence is required");
        }
    }

    public static SettlementCreditEvidence record(Long id, Long organizationId, ApiEnvironment environment,
                                                  Long accountId, String transferReference, Money amount,
                                                  ZonedDateTime receivedAt) {
        ZonedDateTime now = ZonedDateTime.now();
        return new SettlementCreditEvidence(id, organizationId, environment, accountId, transferReference, amount,
                receivedAt == null ? now : receivedAt, null, now);
    }

    public void match(Long settlementId) {
        if (matchedSettlementId != null && !matchedSettlementId.equals(settlementId)) {
            throw new InvalidSettlementStateException("Anchor credit is already matched to another settlement");
        }
        matchedSettlementId = settlementId;
    }

    public boolean isMatched() {
        return matchedSettlementId != null;
    }

    @Override
    public Long getId() {
        return id;
    }
}
