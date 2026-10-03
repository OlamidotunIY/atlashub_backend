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
    private final Set<ComplianceStep> completedSteps;
    private AtlasHubEligibilityStatus eligibilityStatus;
    private AnchorVerificationStatus anchorVerificationStatus;
    private String anchorBusinessCustomerId;
    private String failureCode;
    
    private Long reviewedBy;
    private ZonedDateTime reviewedAt;
    private String rejectionReason;
    
    private ZonedDateTime submittedAt;
    private ZonedDateTime approvedAt;
    
    private BusinessProfileData businessProfile;
    private ContactInfoData contactInfo;
    private OwnerIdentityData ownerIdentity;
    private ComplianceDocumentsData complianceDocuments;
    private ServiceAgreementData serviceAgreement;

    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ComplianceRecord(Long id, Long organizationId, ComplianceStatus status, ComplianceStep currentStep, 
                            Set<ComplianceStep> completedSteps, Long reviewedBy, ZonedDateTime reviewedAt, 
                            String rejectionReason, ZonedDateTime submittedAt, ZonedDateTime approvedAt, 
                            BusinessProfileData businessProfile, ContactInfoData contactInfo, 
                            OwnerIdentityData ownerIdentity, ComplianceDocumentsData complianceDocuments,
                            AtlasHubEligibilityStatus eligibilityStatus,
                            AnchorVerificationStatus anchorVerificationStatus,
                            String anchorBusinessCustomerId, String failureCode,
                            ServiceAgreementData serviceAgreement, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.status = status;
        this.currentStep = currentStep;
        this.completedSteps = completedSteps == null ? new HashSet<>() : new HashSet<>(completedSteps);
        this.eligibilityStatus = eligibilityStatus == null ? AtlasHubEligibilityStatus.PENDING : eligibilityStatus;
        this.anchorVerificationStatus = anchorVerificationStatus == null
                ? AnchorVerificationStatus.NOT_CREATED : anchorVerificationStatus;
        this.anchorBusinessCustomerId = anchorBusinessCustomerId;
        this.failureCode = failureCode;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
        this.submittedAt = submittedAt;
        this.approvedAt = approvedAt;
        this.businessProfile = businessProfile;
        this.contactInfo = contactInfo;
        this.ownerIdentity = ownerIdentity;
        this.complianceDocuments = complianceDocuments;
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
                null, null, null, AtlasHubEligibilityStatus.ELIGIBLE, AnchorVerificationStatus.NOT_CREATED,
                null, null, null, ZonedDateTime.now(), ZonedDateTime.now()
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
        markStepComplete(ComplianceStep.OWNERS_AND_OFFICERS);
    }

    public void updateComplianceDocuments(ComplianceDocumentsData data) {
        ensureCanUpdate();
        ensureStepCompleted(ComplianceStep.OWNERS_AND_OFFICERS);
        this.complianceDocuments = data;
        markStepComplete(ComplianceStep.COMPLIANCE_DOCUMENTS);
    }

    public void acceptServiceAgreement(String ipAddress, ZonedDateTime acceptedAt, String termsVersion) {
        ensureCanUpdate();
        ensureStepCompleted(ComplianceStep.COMPLIANCE_DOCUMENTS);
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
        if (!this.completedSteps.containsAll(Set.of(ComplianceStep.values()))
                || this.businessProfile == null
                || this.contactInfo == null
                || this.ownerIdentity == null
                || this.complianceDocuments == null
                || this.serviceAgreement == null) {
            throw new StepNotCompleteException("All 5 steps must be completed before submission");
        }
        if (this.eligibilityStatus != AtlasHubEligibilityStatus.ELIGIBLE) {
            throw new StepNotCompleteException("Organization is not eligible for the banking programme");
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

    public void markUnderReview(Long ignoredReviewerId) {
        if (this.status != ComplianceStatus.SUBMITTED) {
            throw new StepOutOfOrderException("Compliance must be SUBMITTED before it can be UNDER_REVIEW");
        }
        this.status = ComplianceStatus.UNDER_REVIEW;
        this.anchorVerificationStatus = AnchorVerificationStatus.UNDER_REVIEW;
        this.touch();
    }

    public void recordAnchorApproved() {
        if (this.status != ComplianceStatus.UNDER_REVIEW && this.status != ComplianceStatus.SUBMITTED) {
            throw new StepOutOfOrderException("Compliance must be UNDER_REVIEW or SUBMITTED to be APPROVED");
        }
        if (this.anchorBusinessCustomerId == null || this.anchorBusinessCustomerId.isBlank()) {
            throw new StepOutOfOrderException("Anchor customer must exist before compliance approval");
        }
        this.status = ComplianceStatus.APPROVED;
        this.anchorVerificationStatus = AnchorVerificationStatus.APPROVED;
        this.approvedAt = ZonedDateTime.now();
        this.touch();
        registerEvent(new OrganizationComplianceApprovedEvent(
            UUID.randomUUID().toString(),
            this.id,
            ZonedDateTime.now(),
            CorrelationId.getOrCreate(),
            new OrganizationComplianceApprovedEvent.Payload(this.organizationId, this.anchorBusinessCustomerId, "LIVE", this.approvedAt)
        ));
    }

    public void recordAnchorRejected(String reason) {
        if (this.status != ComplianceStatus.UNDER_REVIEW && this.status != ComplianceStatus.SUBMITTED) {
            throw new StepOutOfOrderException("Compliance must be UNDER_REVIEW or SUBMITTED to be REJECTED");
        }
        this.status = ComplianceStatus.REJECTED;
        this.anchorVerificationStatus = AnchorVerificationStatus.REJECTED;
        this.rejectionReason = reason;
        this.touch();
        registerEvent(new OrganizationComplianceRejectedEvent(
            UUID.randomUUID().toString(),
            this.id,
            ZonedDateTime.now(),
            CorrelationId.getOrCreate(),
            new OrganizationComplianceRejectedEvent.Payload(
                    this.organizationId, this.anchorBusinessCustomerId, this.rejectionReason, ZonedDateTime.now())
        ));
    }

    public void markEligible() {
        ensureCanUpdate();
        this.eligibilityStatus = AtlasHubEligibilityStatus.ELIGIBLE;
        touch();
    }

    public void recordAnchorCustomerCreated(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Anchor customer id is required");
        }
        if (this.anchorBusinessCustomerId != null && !this.anchorBusinessCustomerId.equals(customerId)) {
            throw new StepOutOfOrderException("Anchor customer has already been created");
        }
        this.anchorBusinessCustomerId = customerId;
        this.anchorVerificationStatus = AnchorVerificationStatus.CUSTOMER_CREATED;
        this.status = ComplianceStatus.UNDER_REVIEW;
        touch();
    }

    public void recordAnchorActionRequired(String reason) {
        this.anchorVerificationStatus = AnchorVerificationStatus.AWAITING_DOCUMENTS;
        this.status = ComplianceStatus.ACTION_REQUIRED;
        this.rejectionReason = reason;
        this.completedSteps.remove(ComplianceStep.COMPLIANCE_DOCUMENTS);
        this.currentStep = ComplianceStep.COMPLIANCE_DOCUMENTS;
        touch();
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
