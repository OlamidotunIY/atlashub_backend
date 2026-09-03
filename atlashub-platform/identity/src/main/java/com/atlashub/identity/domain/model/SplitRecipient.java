package com.atlashub.identity.domain.model;

import com.atlashub.identity.domain.event.SplitRecipientDeactivated;
import com.atlashub.identity.domain.event.SplitRecipientRegistered;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import com.atlashub.shared.domain.exception.ValidationException;
import com.atlashub.shared.domain.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class SplitRecipient extends AggregateRoot<Long> {

    private final Long id;
    private final Long OrganizationId;
    private final String bankCode;
    private final String accountNumber;
    private final String accountName;
    private final String description;
    private boolean active;
    private final ZonedDateTime createdAt;

    public SplitRecipient(Long id, Long OrganizationId, String bankCode, String accountNumber, String accountName, String description) {
                if (OrganizationId == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Organization ID is required");
        if (bankCode == null || bankCode.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Bank code is required");
        if (accountNumber == null || accountNumber.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Account number is required");
        if (accountName == null || accountName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Account name is required");

        this.id = id;
        this.OrganizationId = OrganizationId;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.description = description;
        this.active = true;
        this.createdAt = ZonedDateTime.now();

        registerEvent(new SplitRecipientRegistered(
            UUID.randomUUID().toString(),
            id != null ? String.valueOf(id) : null,
            this.createdAt,
            new SplitRecipientRegistered.Payload(
                String.valueOf(OrganizationId),
                this.bankCode,
                this.accountNumber,
                this.accountName
            )
        ));
    }

    // Reconstitution constructor for Mappers
    public SplitRecipient(Long id, Long OrganizationId, String bankCode, String accountNumber, 
                      String accountName, String description, boolean active, ZonedDateTime createdAt) {
        this.id = id;
        this.OrganizationId = OrganizationId;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
    }

    public void deactivate() {
        if (!this.active) {
            throw new BusinessRuleException(IdentityErrorCode.SplitRecipient_ALREADY_INACTIVE, "SplitRecipient is already inactive");
        }
        
        this.active = false;

        registerEvent(new SplitRecipientDeactivated(
            UUID.randomUUID().toString(),
            id != null ? String.valueOf(id) : null,
            ZonedDateTime.now(),
            new SplitRecipientDeactivated.Payload(String.valueOf(OrganizationId))
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
