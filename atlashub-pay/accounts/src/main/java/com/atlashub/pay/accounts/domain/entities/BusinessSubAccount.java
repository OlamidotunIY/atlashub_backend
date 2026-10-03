package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import lombok.Getter;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.time.ZonedDateTime;

@Getter
public class BusinessSubAccount extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private final Long bankingProfileId;
    private final String anchorBusinessCustomerId;
    private final String anchorParentFboAccountId;
    private String anchorSubAccountId;
    private String anchorVirtualNubanId;
    private String accountName;
    private String accountNumber;
    private String maskedAccountNumber;
    private String bankName;
    private String bankCode;
    private final CurrencyCode currency;
    private ExternalAccountStatus status;
    private String failureReason;
    private final ZonedDateTime createdAt;
    private ZonedDateTime activatedAt;
    private ZonedDateTime updatedAt;

    public BusinessSubAccount(Long id, Long organizationId, ApiEnvironment environment, Long bankingProfileId,
                              String anchorBusinessCustomerId, String anchorParentFboAccountId,
                              String anchorSubAccountId, String anchorVirtualNubanId, String accountName,
                              String accountNumber, String maskedAccountNumber, String bankName, String bankCode,
                              CurrencyCode currency, ExternalAccountStatus status, String failureReason,
                              ZonedDateTime createdAt, ZonedDateTime activatedAt, ZonedDateTime updatedAt) {
        this.id=id; this.organizationId=organizationId; this.environment=environment; this.bankingProfileId=bankingProfileId;
        this.anchorBusinessCustomerId=anchorBusinessCustomerId; this.anchorParentFboAccountId=anchorParentFboAccountId;
        this.anchorSubAccountId=anchorSubAccountId; this.anchorVirtualNubanId=anchorVirtualNubanId;
        this.accountName=accountName; this.accountNumber=accountNumber; this.maskedAccountNumber=maskedAccountNumber;
        this.bankName=bankName; this.bankCode=bankCode; this.currency=currency; this.status=status;
        this.failureReason=failureReason; this.createdAt=createdAt; this.activatedAt=activatedAt; this.updatedAt=updatedAt;
    }

    public static BusinessSubAccount request(Long id, Long orgId, ApiEnvironment environment, Long profileId, String customerId,
                                             String parentFboId, CurrencyCode currency) {
        if (parentFboId == null || parentFboId.isBlank()) throw new IllegalArgumentException("AtlasHub FBO account id is required");
        ZonedDateTime now=ZonedDateTime.now();
        return new BusinessSubAccount(id,orgId,environment,profileId,customerId,parentFboId,null,null,
                null,null,null,null,null,currency,ExternalAccountStatus.REQUESTED,null,now,null,now);
    }

    public void markSubmitted(String subAccountId, String virtualNubanId) { this.anchorSubAccountId=subAccountId; this.anchorVirtualNubanId=virtualNubanId; status=ExternalAccountStatus.PENDING; touch(); }
    public void activate(ConfirmedBankingDetails d) { if (status == ExternalAccountStatus.ACTIVE) return; if (d == null) throw new IllegalArgumentException("Confirmed banking details are required"); accountName=d.accountName(); accountNumber=d.accountNumber(); maskedAccountNumber=d.maskedAccountNumber(); bankName=d.bankName(); bankCode=d.bankCode(); status=ExternalAccountStatus.ACTIVE; activatedAt=ZonedDateTime.now(); touch(); }
    public void fail(String reason) { status=ExternalAccountStatus.FAILED; failureReason=reason; touch(); }
    public void suspend(String reason) { status=ExternalAccountStatus.SUSPENDED; failureReason=reason; touch(); }
    public void reactivate() { status=ExternalAccountStatus.ACTIVE; failureReason=null; touch(); }
    private void touch() { updatedAt=ZonedDateTime.now(); }
    @Override public Long getId() { return id; }
}
