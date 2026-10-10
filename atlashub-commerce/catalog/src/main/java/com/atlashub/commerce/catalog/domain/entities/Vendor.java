package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.events.VendorApprovedEvent;
import com.atlashub.commerce.catalog.domain.events.VendorSuspendedEvent;
import com.atlashub.commerce.catalog.domain.events.VendorTerminatedEvent;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.DisbursementSchedule;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Vendor extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long userId;
    private String businessName;
    private EmailAddress email;
    private PhoneNumber phone;
    private String settlementBankCode;
    private String settlementAccountNumber;
    private String settlementAccountName;
    private BigDecimal commissionRate;
    private DisbursementSchedule disbursementSchedule;
    private VendorStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Vendor(Long id, Long organizationId, Long userId, String businessName,
                  EmailAddress email, PhoneNumber phone, String settlementBankCode,
                  String settlementAccountNumber, String settlementAccountName,
                  BigDecimal commissionRate, DisbursementSchedule disbursementSchedule,
                  VendorStatus status, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.businessName = businessName;
        this.email = email;
        this.phone = phone;
        this.settlementBankCode = settlementBankCode;
        this.settlementAccountNumber = settlementAccountNumber;
        this.settlementAccountName = settlementAccountName;
        this.commissionRate = commissionRate;
        this.disbursementSchedule = disbursementSchedule;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validateInvariants();
    }

    public static Vendor create(Long id, Long organizationId, Long userId, String businessName,
                                EmailAddress email, PhoneNumber phone, String settlementBankCode,
                                String settlementAccountNumber, String settlementAccountName,
                                BigDecimal commissionRate, DisbursementSchedule disbursementSchedule) {
        ZonedDateTime now = ZonedDateTime.now();
        return new Vendor(id, organizationId, userId, businessName, email, phone,
                settlementBankCode, settlementAccountNumber, settlementAccountName,
                commissionRate, disbursementSchedule, VendorStatus.PENDING, now, now);
    }

    public void approve() {
        if (this.status == VendorStatus.ACTIVE) {
            return;
        }
        if (this.status != VendorStatus.PENDING) {
            throw new InvalidProductStateException("Only pending vendors can be approved");
        }
        this.status = VendorStatus.ACTIVE;
        touch();
        registerEvent(new VendorApprovedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new VendorApprovedEvent.Payload(this.organizationId, this.userId, this.businessName, this.updatedAt)
        ));
    }

    public void suspend(String reason) {
        if (this.status == VendorStatus.SUSPENDED) {
            return;
        }
        if (this.status == VendorStatus.TERMINATED) {
            throw new InvalidProductStateException("Cannot suspend a terminated vendor");
        }
        this.status = VendorStatus.SUSPENDED;
        touch();
        registerEvent(new VendorSuspendedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new VendorSuspendedEvent.Payload(this.organizationId, this.userId, this.businessName,
                        reason != null ? reason.trim() : "Vendor suspended", this.updatedAt)
        ));
    }

    public void terminate() {
        if (this.status == VendorStatus.TERMINATED) {
            return;
        }
        this.status = VendorStatus.TERMINATED;
        touch();
        registerEvent(new VendorTerminatedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new VendorTerminatedEvent.Payload(this.organizationId, this.userId, this.businessName, this.updatedAt)
        ));
    }

    public void updateSettlementDetails(String bankCode, String accountNumber, String accountName) {
        if (this.status == VendorStatus.TERMINATED) {
            throw new InvalidProductStateException("Cannot update settlement details for a terminated vendor");
        }
        this.settlementBankCode = bankCode;
        this.settlementAccountNumber = accountNumber;
        this.settlementAccountName = accountName;
        touch();
    }

    public void updateCommissionRate(BigDecimal commissionRate) {
        if (commissionRate == null || commissionRate.signum() < 0 || commissionRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new InvalidProductStateException("Commission rate must be between 0 and 100%");
        }
        this.commissionRate = commissionRate;
        touch();
    }

    public boolean isActive() {
        return this.status == VendorStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == VendorStatus.SUSPENDED;
    }

    public boolean isTerminated() {
        return this.status == VendorStatus.TERMINATED;
    }

    private void validateInvariants() {
        if (id == null) {
            throw new InvalidProductStateException("Vendor id cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidProductStateException("Organization id cannot be null");
        }
        if (userId == null) {
            throw new InvalidProductStateException("User id cannot be null for vendor");
        }
        if (businessName == null || businessName.isBlank()) {
            throw new InvalidProductStateException("Business name is required for vendor");
        }
        if (commissionRate != null && (commissionRate.signum() < 0 || commissionRate.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new InvalidProductStateException("Commission rate must be between 0 and 100%");
        }
        if (status == null) {
            throw new InvalidProductStateException("Vendor status cannot be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new InvalidProductStateException("Timestamps cannot be null");
        }
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
