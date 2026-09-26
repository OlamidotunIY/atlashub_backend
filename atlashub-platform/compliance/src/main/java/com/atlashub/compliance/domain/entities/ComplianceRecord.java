package com.atlashub.compliance.domain.entities;

import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.compliance.domain.exception.*;
import com.atlashub.compliance.domain.events.*;
import com.atlashub.shared.domain.valueobject.CorrelationId;

import lombok.Getter;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class ComplianceRecord extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private ComplianceStatus status;
    private ComplianceStep currentStep;
    private Set<ComplianceStep> completedSteps;
    
    private Long reviewedBy;
    private ZonedDateTime reviewedAt;
    private String rejectionReason;
    
    private ZonedDateTime submittedAt;
    private ZonedDateTime approvedAt;
    
    private BusinessProfileData businessProfile;
    private ContactInfoData contactInfo;
    private OwnerIdentityData ownerIdentity;
    private SettlementAccountData settlementAccount;
    private ServiceAgreementData serviceAgreement;

    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ComplianceRecord(Long id, Long organizationId, ComplianceStatus status, ComplianceStep currentStep, 
                            Set<ComplianceStep> completedSteps, Long reviewedBy, ZonedDateTime reviewedAt, 
                            String rejectionReason, ZonedDateTime submittedAt, ZonedDateTime approvedAt, 
                            BusinessProfileData businessProfile, ContactInfoData contactInfo, 
                            OwnerIdentityData ownerIdentity, SettlementAccountData settlementAccount, 
                            ServiceAgreementData serviceAgreement, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.status = status;
        this.currentStep = currentStep;
        this.completedSteps = completedSteps == null ? new HashSet<>() : new HashSet<>(completedSteps);
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
        this.submittedAt = submittedAt;
        this.approvedAt = approvedAt;
        this.businessProfile = businessProfile;
        this.contactInfo = contactInfo;
        this.ownerIdentity = ownerIdentity;
        this.settlementAccount = settlementAccount;
        this.serviceAgreement = serviceAgreement;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ComplianceRecord create(Long id, Long organizationId) {
        if (id == null || organizationId == null) {
            throw new IllegalArgumentException("id and organizationId cannot be null");
        }
        ComplianceRecord record = new ComplianceRecord(
                id, organizationId, ComplianceStatus.NOT_STARTED, null, 
                new HashSet<>(), null, null, null, null, null, null, 
                null, null, null, null, ZonedDateTime.now(), ZonedDateTime.now()
        );
        record.registerEvent(new ComplianceRecordInitializedEvent(
            UUID.randomUUID().toString(),
            id,
            ZonedDateTime.now(),
            CorrelationId.getOrCreate(),
            new ComplianceRecordInitializedEvent.Payload(organizationId)
        ));
        return record;
    }

    public void updateBusinessProfile(BusinessProfileData data) {
        ensureCanUpdate();
        this.businessProfile = data;
        markStepComplete(ComplianceStep.BUSINESS_PROFILE);
    }

    public void updateContactInfo(ContactInfoData data) {
        ensureCanUpdate();
        ensureStepCompleted(ComplianceStep.BUSINESS_PROFILE);
        this.contactInfo = data;
        markStepComplete(ComplianceStep.CONTACT_INFO);
    }

    public void updateOwnerIdentity(OwnerIdentityData data) {
        ensureCanUpdate();
        ensureStepCompleted(ComplianceStep.CONTACT_INFO);
        this.ownerIdentity = data;
        markStepComplete(ComplianceStep.OWNER_IDENTITY);
    }

    public void updateSettlementAccount(SettlementAccountData data) {
        ensureCanUpdate();
        ensureStepCompleted(ComplianceStep.OWNER_IDENTITY);
        this.settlementAccount = data;
        markStepComplete(ComplianceStep.SETTLEMENT_ACCOUNT);
    }

    public void acceptServiceAgreement(String ipAddress, ZonedDateTime acceptedAt, String termsVersion) {
        ensureCanUpdate();
        ensureStepCompleted(ComplianceStep.SETTLEMENT_ACCOUNT);
        this.serviceAgreement = new ServiceAgreementData(acceptedAt, ipAddress, termsVersion);
        markStepComplete(ComplianceStep.SERVICE_AGREEMENT);
    }

    public void submit() {
        if (this.status == ComplianceStatus.SUBMITTED) {
            throw new ComplianceAlreadySubmittedException("Compliance is already submitted");
        }
        if (this.status == ComplianceStatus.APPROVED) {
            throw new ComplianceAlreadyApprovedException("Compliance is already approved");
        }
        if (this.completedSteps.size() != 5) {
            throw new StepNotCompleteException("All 5 steps must be completed before submission");
        }
        this.status = ComplianceStatus.SUBMITTED;
        this.submittedAt = ZonedDateTime.now();
        this.touch();
        registerEvent(new ComplianceSubmittedEvent(
            UUID.randomUUID().toString(),
            this.id,
            ZonedDateTime.now(),
            CorrelationId.getOrCreate(),
            new ComplianceSubmittedEvent.Payload(this.organizationId, this.submittedAt)
        ));
    }

    public void markUnderReview(Long adminId) {
        if (this.status != ComplianceStatus.SUBMITTED) {
            throw new StepOutOfOrderException("Compliance must be SUBMITTED before it can be UNDER_REVIEW");
        }
        this.status = ComplianceStatus.UNDER_REVIEW;
        this.reviewedBy = adminId;
        this.reviewedAt = ZonedDateTime.now();
        this.touch();
    }

    public void approve(Long adminId) {
        if (this.status != ComplianceStatus.UNDER_REVIEW && this.status != ComplianceStatus.SUBMITTED) {
            throw new StepOutOfOrderException("Compliance must be UNDER_REVIEW or SUBMITTED to be APPROVED");
        }
        this.status = ComplianceStatus.APPROVED;
        this.reviewedBy = adminId;
        this.reviewedAt = ZonedDateTime.now();
        this.approvedAt = ZonedDateTime.now();
        this.touch();
        registerEvent(new OrganizationComplianceApprovedEvent(
            UUID.randomUUID().toString(),
            this.id,
            ZonedDateTime.now(),
            CorrelationId.getOrCreate(),
            new OrganizationComplianceApprovedEvent.Payload(this.organizationId, this.reviewedBy, this.approvedAt)
        ));
    }

    public void reject(Long adminId, String reason) {
        if (this.status != ComplianceStatus.UNDER_REVIEW && this.status != ComplianceStatus.SUBMITTED) {
            throw new StepOutOfOrderException("Compliance must be UNDER_REVIEW or SUBMITTED to be REJECTED");
        }
        this.status = ComplianceStatus.REJECTED;
        this.reviewedBy = adminId;
        this.reviewedAt = ZonedDateTime.now();
        this.rejectionReason = reason;
        this.touch();
        registerEvent(new OrganizationComplianceRejectedEvent(
            UUID.randomUUID().toString(),
            this.id,
            ZonedDateTime.now(),
            CorrelationId.getOrCreate(),
            new OrganizationComplianceRejectedEvent.Payload(this.organizationId, this.reviewedBy, this.rejectionReason, this.reviewedAt)
        ));
    }

    public void reopen() {
        if (this.status != ComplianceStatus.REJECTED && this.status != ComplianceStatus.APPROVED) {
            throw new StepOutOfOrderException("Only REJECTED or APPROVED compliance can be reopened");
        }
        this.status = ComplianceStatus.IN_PROGRESS;
        this.rejectionReason = null;
        this.touch();
    }

    private void ensureCanUpdate() {
        if (this.status == ComplianceStatus.APPROVED) {
            throw new ComplianceAlreadyApprovedException("Compliance is already approved and cannot be updated");
        }
        if (this.status == ComplianceStatus.SUBMITTED || this.status == ComplianceStatus.UNDER_REVIEW) {
            throw new ComplianceAlreadySubmittedException("Compliance is currently under review and cannot be updated");
        }
    }

    private void ensureStepCompleted(ComplianceStep step) {
        if (!this.completedSteps.contains(step)) {
            throw new StepOutOfOrderException("Step " + step + " must be completed first");
        }
    }

    private void markStepComplete(ComplianceStep step) {
        this.completedSteps.add(step);
        this.currentStep = step;
        if (this.status == ComplianceStatus.NOT_STARTED) {
            this.status = ComplianceStatus.IN_PROGRESS;
        }
        this.touch();
        registerEvent(new ComplianceStepCompletedEvent(
            UUID.randomUUID().toString(),
            this.id,
            ZonedDateTime.now(),
            CorrelationId.getOrCreate(),
            new ComplianceStepCompletedEvent.Payload(this.organizationId, step.name())
        ));
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
