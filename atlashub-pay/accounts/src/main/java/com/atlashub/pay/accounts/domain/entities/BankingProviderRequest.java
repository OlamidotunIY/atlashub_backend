package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingAccountDataException;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class BankingProviderRequest extends AggregateRoot<Long> {
    public enum RequestType { DEPOSIT, SUB_ACCOUNT, RESERVED_ACCOUNT, FREEZE_DEPOSIT, UNFREEZE_DEPOSIT }
    public enum RequestStatus { PENDING, COMPLETED, FAILED }

    private final Long id;
    private final RequestType requestType;
    private final Long aggregateId;
    private final String requestReference;
    private final String apiEnvironment;
    private final String anchorCustomerId;
    private final String parentOrPayoutAccountId;
    private final String provider;
    private final String customerType;
    private final String customerReferenceId;
    private final String customerFullName;
    private final String customerEmail;
    private final String customerBvn;
    private final String operationReason;
    private RequestStatus status;
    private int attempts;
    private String failureReason;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public BankingProviderRequest(Long id, RequestType requestType, Long aggregateId,
            String requestReference, String apiEnvironment, String anchorCustomerId, String parentOrPayoutAccountId,
            String provider, String customerType, String customerReferenceId,
            String customerFullName, String customerEmail, String customerBvn,
            String operationReason,
            RequestStatus status, int attempts, String failureReason,
            ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id; this.requestType = requestType; this.aggregateId = aggregateId;
        this.requestReference = requestReference; this.apiEnvironment = requireApiEnvironment(apiEnvironment); this.anchorCustomerId = anchorCustomerId;
        this.parentOrPayoutAccountId = parentOrPayoutAccountId; this.provider = provider;
        this.customerType = customerType; this.customerReferenceId = customerReferenceId;
        this.customerFullName = customerFullName; this.customerEmail = customerEmail;
        this.customerBvn = customerBvn; this.operationReason = operationReason;
        this.status = status; this.attempts = attempts;
        this.failureReason = failureReason; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public static BankingProviderRequest create(Long id, RequestType type, Long aggregateId,
            String requestReference, String apiEnvironment, String anchorCustomerId, String parentOrPayoutAccountId,
            String provider, String customerType, String customerReferenceId,
            String customerFullName, String customerEmail, String customerBvn) {
        ZonedDateTime now = ZonedDateTime.now();
        return new BankingProviderRequest(id, type, aggregateId, requestReference, apiEnvironment, anchorCustomerId,
                parentOrPayoutAccountId, provider, customerType, customerReferenceId,
                customerFullName, customerEmail, customerBvn, null, RequestStatus.PENDING,
                0, null, now, now);
    }

    public static BankingProviderRequest createDepositLifecycle(Long id, RequestType type, Long aggregateId,
            String requestReference, String apiEnvironment, String reason) {
        if (type != RequestType.FREEZE_DEPOSIT && type != RequestType.UNFREEZE_DEPOSIT)
            throw new InvalidBankingAccountDataException("Deposit lifecycle request type is required");
        ZonedDateTime now = ZonedDateTime.now();
        return new BankingProviderRequest(id, type, aggregateId, requestReference, apiEnvironment,
                null, null, null, null, null, null, null, null, reason,
                RequestStatus.PENDING, 0, null, now, now);
    }

    public void complete() { status = RequestStatus.COMPLETED; failureReason = null; updatedAt = ZonedDateTime.now(); }
    public void fail(String reason) { attempts++; failureReason = reason; status = attempts >= 5
            ? RequestStatus.FAILED : RequestStatus.PENDING; updatedAt = ZonedDateTime.now(); }
    @Override public Long getId() { return id; }

    private static String requireApiEnvironment(String apiEnvironment) {
        if (apiEnvironment == null || (!"TEST".equalsIgnoreCase(apiEnvironment) && !"LIVE".equalsIgnoreCase(apiEnvironment))) {
            throw new InvalidBankingAccountDataException("Banking provider request environment must be TEST or LIVE");
        }
        return apiEnvironment.toUpperCase();
    }
}
