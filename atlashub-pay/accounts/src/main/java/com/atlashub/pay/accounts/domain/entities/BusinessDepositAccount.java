package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import lombok.Getter;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.time.ZonedDateTime;

@Getter
public class BusinessDepositAccount extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private final Long bankingProfileId;
    private final String anchorBusinessCustomerId;
    private String anchorAccountId;
    private String accountName;
    private String accountNumber;
    private String maskedAccountNumber;
    private String bankName;
    private String bankCode;
    private final CurrencyCode currency;
    private boolean frozen;
    private ExternalAccountStatus status;
    private String failureReason;
    private final ZonedDateTime createdAt;
    private ZonedDateTime activatedAt;
    private ZonedDateTime updatedAt;

    public BusinessDepositAccount(Long id, Long organizationId, ApiEnvironment environment, Long bankingProfileId,
                                  String anchorBusinessCustomerId, String anchorAccountId,
                                  String accountName, String accountNumber, String maskedAccountNumber,
                                  String bankName, String bankCode, CurrencyCode currency, boolean frozen,
                                  ExternalAccountStatus status, String failureReason, ZonedDateTime createdAt,
                                  ZonedDateTime activatedAt, ZonedDateTime updatedAt) {
        this.id=id; this.organizationId=organizationId; this.environment=environment; this.bankingProfileId=bankingProfileId;
        this.anchorBusinessCustomerId=anchorBusinessCustomerId; this.anchorAccountId=anchorAccountId;
        this.accountName=accountName; this.accountNumber=accountNumber; this.maskedAccountNumber=maskedAccountNumber;
        this.bankName=bankName; this.bankCode=bankCode; this.currency=currency; this.frozen=frozen;
        this.status=status; this.failureReason=failureReason; this.createdAt=createdAt;
        this.activatedAt=activatedAt; this.updatedAt=updatedAt;
    }

    public static BusinessDepositAccount request(Long id, Long organizationId, ApiEnvironment environment, Long profileId,
                                                 String anchorCustomerId, CurrencyCode currency) {
        ZonedDateTime now=ZonedDateTime.now();
        return new BusinessDepositAccount(id, organizationId, environment, profileId, anchorCustomerId, null,
                null,null,null,null,null,currency,false,ExternalAccountStatus.REQUESTED,null,now,null,now);
    }

    public void markSubmitted(String anchorAccountId) {
        if (anchorAccountId == null || anchorAccountId.isBlank()) throw new IllegalArgumentException("Anchor account id is required");
        this.anchorAccountId=anchorAccountId; this.status=ExternalAccountStatus.PENDING; touch();
    }

    public void activate(ConfirmedBankingDetails details) {
        if (status == ExternalAccountStatus.ACTIVE) return;
        if (status != ExternalAccountStatus.PENDING && status != ExternalAccountStatus.REQUESTED)
            throw new IllegalStateException("Only requested or pending accounts can be activated");
        if (details == null) throw new IllegalArgumentException("Confirmed banking details are required");
        apply(details); status=ExternalAccountStatus.ACTIVE; activatedAt=ZonedDateTime.now(); touch();
    }

    public void freeze(String reason) { frozen=true; status=ExternalAccountStatus.FROZEN; failureReason=reason; touch(); }
    public void reactivate() { frozen=false; status=ExternalAccountStatus.ACTIVE; failureReason=null; touch(); }
    public void fail(String reason) { status=ExternalAccountStatus.FAILED; failureReason=reason; touch(); }
    private void apply(ConfirmedBankingDetails d) { accountName=d.accountName(); accountNumber=d.accountNumber(); maskedAccountNumber=d.maskedAccountNumber(); bankName=d.bankName(); bankCode=d.bankCode(); }
    private void touch() { updatedAt=ZonedDateTime.now(); }
    @Override public Long getId() { return id; }
}
