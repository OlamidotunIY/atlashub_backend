package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.events.VirtualAccountActivatedEvent;
import com.atlashub.pay.accounts.domain.events.VirtualAccountClosedEvent;
import com.atlashub.pay.accounts.domain.events.VirtualAccountSuspendedEvent;
import com.atlashub.pay.accounts.domain.exceptions.VirtualAccountAlreadyActiveException;
import com.atlashub.pay.accounts.domain.exceptions.VirtualAccountNotActiveException;
import com.atlashub.pay.accounts.domain.valueobject.OwnerType;
import com.atlashub.pay.accounts.domain.valueobject.VirtualAccountStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class VirtualAccount extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private OwnerType ownerType;
    private final Long customerId;
    private String accountName;
    private String bankName;
    private String nuban;
    private String bankProvider;
    private String anchorAccountId;
    private CurrencyCode currency;
    private VirtualAccountStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime activatedAt;


    public VirtualAccount(Long id, Long organizationId, OwnerType ownerType, Long customerId, String accountName, String bankName, String nuban, String bankProvider, String anchorAccountId, CurrencyCode currency, VirtualAccountStatus status, ZonedDateTime createdAt, ZonedDateTime activatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.ownerType = ownerType;
        this.customerId = customerId;
        this.accountName = accountName;
        this.bankName = bankName;
        this.nuban = nuban;
        this.bankProvider = bankProvider;
        this.anchorAccountId = anchorAccountId;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.activatedAt = activatedAt;
    }

    public static VirtualAccount create(Long id, Long organizationId, OwnerType ownerType, Long customerId, String accountName, CurrencyCode currency) {
        ZonedDateTime now = ZonedDateTime.now();

        return new VirtualAccount(id, organizationId, ownerType, customerId, accountName, null, null, null, null, currency, VirtualAccountStatus.PENDING_ISSUANCE, now, null);
    }

    public void activate(String bankName, String nuban, String bankProvider, String anchorAccountId) {
        if (this.status != VirtualAccountStatus.PENDING_ISSUANCE) {
            throw new VirtualAccountAlreadyActiveException();
        }

        ZonedDateTime now = ZonedDateTime.now();

        this.bankProvider = bankProvider;
        this.bankName = bankName;
        this.nuban = nuban;
        this.anchorAccountId = anchorAccountId;
        this.activatedAt = now;

        VirtualAccountActivatedEvent.Payload payload = new VirtualAccountActivatedEvent.Payload(id, this.organizationId, this.ownerType.toString(), this.customerId.toString(), this.nuban, this.bankName, this.bankProvider, this.currency, this.accountName);

        String eventId = UUID.randomUUID().toString();

        registerEvent(
                new VirtualAccountActivatedEvent(
                        eventId,
                        id,
                        now,
                        CorrelationId.getOrCreate(),
                        payload
                )
        );
    }

    public void suspend() {
        if (this.status != VirtualAccountStatus.ACTIVE) {
            throw new VirtualAccountNotActiveException();
        }

        this.status = VirtualAccountStatus.SUSPENDED;

        String eventId = UUID.randomUUID().toString();

        registerEvent(new VirtualAccountSuspendedEvent(
                eventId,
                id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                null
        ));
    }

    public void close() {
        if ((this.status != VirtualAccountStatus.ACTIVE) && (this.status != VirtualAccountStatus.SUSPENDED)) {
            throw new VirtualAccountNotActiveException();
        }
        this.status = VirtualAccountStatus.CLOSED;

        String eventId = UUID.randomUUID().toString();

        registerEvent(new VirtualAccountClosedEvent(
                eventId,
                id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                null
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
