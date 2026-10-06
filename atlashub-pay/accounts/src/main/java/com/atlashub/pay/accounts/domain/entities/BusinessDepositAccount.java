package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import lombok.Getter;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingAccountDataException;
import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingStateException;
import com.atlashub.pay.accounts.domain.events.BusinessDepositAccountActivatedEvent;
import com.atlashub.pay.accounts.domain.events.OrganizationAccountFundedEvent;
import com.atlashub.shared.domain.valueobject.CorrelationId;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

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
        if (anchorAccountId == null || anchorAccountId.isBlank()) throw new InvalidBankingAccountDataException("Anchor account id is required");
        this.anchorAccountId=anchorAccountId; this.status=ExternalAccountStatus.PENDING; touch();
    }

    public void activate(ConfirmedBankingDetails details) {
        if (status == ExternalAccountStatus.ACTIVE) return;
        if (status != ExternalAccountStatus.PENDING && status != ExternalAccountStatus.REQUESTED)
            throw new InvalidBankingStateException("Only requested or pending accounts can be activated");
        if (details == null || !details.hasAccountNumberDetails())
            throw new InvalidBankingAccountDataException("Confirmed deposit-account name and number are required");
        apply(details); status=ExternalAccountStatus.ACTIVE; activatedAt=ZonedDateTime.now(); touch();
        registerEvent(new BusinessDepositAccountActivatedEvent(
                UUID.randomUUID().toString(), id, ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new BusinessDepositAccountActivatedEvent.Payload(
                        organizationId, bankingProfileId, environment.name(), currency.name(), activatedAt)));
    }

    public void recordFunding(String transferReference, BigDecimal amount, CurrencyCode fundingCurrency,
                              ZonedDateTime receivedAt) {
        if (transferReference == null || transferReference.isBlank() || amount == null
                || amount.signum() <= 0 || fundingCurrency == null) {
            throw new InvalidBankingAccountDataException("Valid funding reference, amount and currency are required");
        }
        registerEvent(new OrganizationAccountFundedEvent(
                UUID.randomUUID().toString(), id, ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new OrganizationAccountFundedEvent.Payload(organizationId, environment.name(), id,
                        transferReference, amount, fundingCurrency.name(),
                        receivedAt == null ? ZonedDateTime.now() : receivedAt)));
    }

    public void freeze(String reason) { frozen=true; status=ExternalAccountStatus.FROZEN; failureReason=reason; touch(); }
    public void reactivate() { frozen=false; status=ExternalAccountStatus.ACTIVE; failureReason=null; touch(); }
    public void fail(String reason) { status=ExternalAccountStatus.FAILED; failureReason=reason; touch(); }
    private void apply(ConfirmedBankingDetails d) { accountName=d.accountName(); accountNumber=d.accountNumber(); maskedAccountNumber=d.maskedAccountNumber(); bankName=d.bankName(); bankCode=d.bankCode(); }
    private void touch() { updatedAt=ZonedDateTime.now(); }
    @Override public Long getId() { return id; }
}
