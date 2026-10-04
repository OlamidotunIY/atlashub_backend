package com.atlashub.compliance.domain.entities;

import com.atlashub.compliance.domain.events.*;
import com.atlashub.compliance.domain.exception.*;
import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.*;

@Getter
public class ComplianceRecord extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private ComplianceStatus status;
    private ComplianceStep currentStep;
    private final EnumMap<ComplianceStep, StepStatus> stepProgress;
    private AtlasHubEligibilityStatus eligibilityStatus;
    private AnchorVerificationStatus anchorVerificationStatus;
    private String anchorBusinessCustomerId;
    private String failureCode;
    private String rejectionReason;
    private ZonedDateTime submittedAt;
    private ZonedDateTime approvedAt;
    private BusinessProfileData businessProfile;
    private ContactInfoData contactInfo;
    private final List<BusinessOfficer> officers;
    private final List<ComplianceDocumentRequirement> documentRequirements;
    private ServiceAgreementData serviceAgreement;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ComplianceRecord(Long id, Long organizationId, ComplianceStatus status, ComplianceStep currentStep,
                            Map<ComplianceStep, StepStatus> stepProgress,
                            AtlasHubEligibilityStatus eligibilityStatus,
                            AnchorVerificationStatus anchorVerificationStatus, String anchorBusinessCustomerId,
                            String failureCode, String rejectionReason, ZonedDateTime submittedAt,
                            ZonedDateTime approvedAt, BusinessProfileData businessProfile, ContactInfoData contactInfo,
                            List<BusinessOfficer> officers, List<ComplianceDocumentRequirement> documentRequirements,
                            ServiceAgreementData serviceAgreement, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        if (id == null || organizationId == null) throw new InvalidComplianceDataException("Compliance identity is required");
        this.id = id; this.organizationId = organizationId;
        this.status = status == null ? ComplianceStatus.NOT_STARTED : status;
        this.currentStep = currentStep == null ? ComplianceStep.BUSINESS_PROFILE : currentStep;
        this.stepProgress = defaultProgress();
        if (stepProgress != null) this.stepProgress.putAll(stepProgress);
        this.eligibilityStatus = eligibilityStatus == null ? AtlasHubEligibilityStatus.PENDING : eligibilityStatus;
        this.anchorVerificationStatus = anchorVerificationStatus == null ? AnchorVerificationStatus.NOT_CREATED : anchorVerificationStatus;
        this.anchorBusinessCustomerId = anchorBusinessCustomerId; this.failureCode = failureCode;
        this.rejectionReason = rejectionReason; this.submittedAt = submittedAt; this.approvedAt = approvedAt;
        this.businessProfile = businessProfile; this.contactInfo = contactInfo;
        this.officers = new ArrayList<>(officers == null ? List.of() : officers);
        this.documentRequirements = new ArrayList<>(documentRequirements == null ? List.of() : documentRequirements);
        this.serviceAgreement = serviceAgreement;
        this.createdAt = createdAt == null ? ZonedDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public static ComplianceRecord create(Long id, Long organizationId) {
        ComplianceRecord record = new ComplianceRecord(id, organizationId, ComplianceStatus.NOT_STARTED,
                ComplianceStep.BUSINESS_PROFILE, null, AtlasHubEligibilityStatus.PENDING,
                AnchorVerificationStatus.NOT_CREATED, null, null, null, null, null,
                null, null, null, null, null, ZonedDateTime.now(), ZonedDateTime.now());
        record.registerEvent(new ComplianceRecordInitializedEvent(UUID.randomUUID().toString(), id,
                ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new ComplianceRecordInitializedEvent.Payload(organizationId)));
        return record;
    }

    public void updateBusinessRegistration(BusinessProfileData data, boolean eligible) {
        ensureCanUpdate();
        if (data == null) throw new InvalidComplianceDataException("Business profile is required");
        businessProfile = data;
        eligibilityStatus = eligible ? AtlasHubEligibilityStatus.ELIGIBLE : AtlasHubEligibilityStatus.INELIGIBLE;
        complete(ComplianceStep.BUSINESS_PROFILE);
    }

    public void updateContactAndAddresses(ContactInfoData data) {
        ensureCanUpdate(); requireComplete(ComplianceStep.BUSINESS_PROFILE);
        if (data == null) throw new InvalidComplianceDataException("Contact information is required");
        contactInfo = data; complete(ComplianceStep.CONTACT_INFO);
    }

    public void replaceOfficers(List<BusinessOfficer> replacements) {
        ensureCanUpdate(); requireComplete(ComplianceStep.CONTACT_INFO);
        if (replacements == null || replacements.isEmpty() || replacements.stream().noneMatch(o -> o.getRole() == OfficerRole.OWNER))
            throw new InvalidComplianceDataException("At least one business owner is required");
        officers.clear(); officers.addAll(replacements); complete(ComplianceStep.OWNERS_AND_OFFICERS);
    }

    public void recordRequiredDocuments(List<ComplianceDocumentRequirement> requirements) {
        if (requirements == null) throw new InvalidComplianceDataException("Document requirements are required");
        for (ComplianceDocumentRequirement incoming : requirements) {
            documentRequirements.stream().filter(current -> current.getDocumentType().equals(incoming.getDocumentType()))
                    .findFirst().ifPresentOrElse(current -> {
                        if (incoming.getAnchorDocumentId() != null)
                            current.identifyByAnchor(incoming.getAnchorDocumentId(), incoming.getDescription());
                    }, () -> documentRequirements.add(incoming));
        }
        refreshDocumentStep();
    }

    public void saveComplianceDocument(Long requirementId, String objectKey, String textValue) {
        ensureCanUpdate(); requireComplete(ComplianceStep.OWNERS_AND_OFFICERS);
        requirement(requirementId).save(objectKey, textValue); refreshDocumentStep();
    }

    public void acceptServiceAgreement(String ipAddress, ZonedDateTime acceptedAt, String termsVersion) {
        ensureCanUpdate(); requireComplete(ComplianceStep.COMPLIANCE_DOCUMENTS);
        serviceAgreement = new ServiceAgreementData(acceptedAt, ipAddress, termsVersion);
        complete(ComplianceStep.SERVICE_AGREEMENT);
    }

    public void submit() {
        if (status == ComplianceStatus.SUBMITTED || status == ComplianceStatus.UNDER_REVIEW)
            throw new ComplianceAlreadySubmittedException("Compliance is already submitted");
        if (status == ComplianceStatus.APPROVED) throw new ComplianceAlreadyApprovedException("Compliance is already approved");
        if (Arrays.stream(ComplianceStep.values()).anyMatch(step -> stepProgress.get(step) != StepStatus.COMPLETE))
            throw new StepNotCompleteException("All five compliance steps must be complete");
        if (eligibilityStatus != AtlasHubEligibilityStatus.ELIGIBLE)
            throw new StepNotCompleteException("Organization is not eligible for the banking programme");
        status = ComplianceStatus.SUBMITTED; submittedAt = ZonedDateTime.now(); touch();
        registerEvent(new ComplianceSubmittedEvent(UUID.randomUUID().toString(), id, ZonedDateTime.now(),
                CorrelationId.getOrCreate(), new ComplianceSubmittedEvent.Payload(organizationId, submittedAt)));
    }

    public void recordAnchorCustomerCreated(String customerId, Map<Long, String> officerMappings) {
        requireText(customerId, "Anchor customer id");
        if (anchorBusinessCustomerId != null && !anchorBusinessCustomerId.equals(customerId))
            throw new StepOutOfOrderException("Anchor customer has already been created");
        anchorBusinessCustomerId = customerId;
        if (officerMappings != null) officers.forEach(officer -> {
            String anchorId = officerMappings.get(officer.getId());
            if (anchorId != null) officer.mapToAnchor(anchorId);
        });
        anchorVerificationStatus = AnchorVerificationStatus.CUSTOMER_CREATED;
        status = ComplianceStatus.UNDER_REVIEW; touch();
    }

    public void recordVerificationTriggered() {
        if (anchorBusinessCustomerId == null) throw new StepOutOfOrderException("Anchor customer must be created first");
        anchorVerificationStatus = AnchorVerificationStatus.VERIFICATION_TRIGGERED;
        status = ComplianceStatus.UNDER_REVIEW; touch();
    }

    public void recordDocumentUnderReview(Long requirementId) { requirement(requirementId).markUnderReview(); refreshDocumentStep(); }
    public void recordDocumentApproved(String anchorDocumentId) { requirementByAnchorId(anchorDocumentId).approve(); refreshDocumentStep(); }
    public void recordDocumentRejected(String anchorDocumentId, String reason) {
        requirementByAnchorId(anchorDocumentId).reject(reason); actionRequired(reason);
    }

    public void recordAnchorApproved() {
        if (anchorBusinessCustomerId == null) throw new StepOutOfOrderException("Anchor customer must exist before approval");
        if (eligibilityStatus != AtlasHubEligibilityStatus.ELIGIBLE || serviceAgreement == null)
            throw new StepOutOfOrderException("AtlasHub eligibility and agreement are required for approval");
        boolean newlyApproved = status != ComplianceStatus.APPROVED;
        anchorVerificationStatus = AnchorVerificationStatus.APPROVED; status = ComplianceStatus.APPROVED;
        approvedAt = ZonedDateTime.now(); failureCode = null; rejectionReason = null; touch();
        if (newlyApproved) registerEvent(new OrganizationComplianceApprovedEvent(UUID.randomUUID().toString(), id,
                ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new OrganizationComplianceApprovedEvent.Payload(organizationId, anchorBusinessCustomerId, "LIVE", approvedAt)));
    }

    public void recordAnchorRejected(String reason) {
        requireText(reason, "Anchor rejection reason");
        anchorVerificationStatus = AnchorVerificationStatus.REJECTED; status = ComplianceStatus.REJECTED;
        rejectionReason = reason; touch();
        registerEvent(new OrganizationComplianceRejectedEvent(UUID.randomUUID().toString(), id, ZonedDateTime.now(),
                CorrelationId.getOrCreate(), new OrganizationComplianceRejectedEvent.Payload(
                organizationId, anchorBusinessCustomerId, reason, ZonedDateTime.now())));
    }

    public void recordAnchorError(String code, String message) {
        requireText(code, "Provider failure code"); requireText(message, "Provider failure message");
        anchorVerificationStatus = AnchorVerificationStatus.ERROR; failureCode = code; rejectionReason = message;
        status = ComplianceStatus.ACTION_REQUIRED; touch();
        registerEvent(new ComplianceProviderErrorEvent(UUID.randomUUID().toString(), id, ZonedDateTime.now(),
                CorrelationId.getOrCreate(), new ComplianceProviderErrorEvent.Payload(organizationId, code, message)));
    }

    public void suspend(String reason) {
        if (status != ComplianceStatus.APPROVED) throw new StepOutOfOrderException("Only approved compliance can be suspended");
        requireText(reason, "Suspension reason"); status = ComplianceStatus.SUSPENDED; rejectionReason = reason; touch();
        registerEvent(new OrganizationComplianceSuspendedEvent(UUID.randomUUID().toString(), id, ZonedDateTime.now(),
                CorrelationId.getOrCreate(), new OrganizationComplianceSuspendedEvent.Payload(
                organizationId, anchorBusinessCustomerId, reason, ZonedDateTime.now())));
    }

    public void reinstate(String reason) {
        if (status != ComplianceStatus.SUSPENDED || anchorVerificationStatus != AnchorVerificationStatus.APPROVED)
            throw new StepOutOfOrderException("Only provider-approved suspended compliance can be reinstated");
        requireText(reason, "Reinstatement reason"); status = ComplianceStatus.APPROVED; rejectionReason = null; touch();
        registerEvent(new OrganizationComplianceReinstatedEvent(UUID.randomUUID().toString(), id, ZonedDateTime.now(),
                CorrelationId.getOrCreate(), new OrganizationComplianceReinstatedEvent.Payload(
                organizationId, anchorBusinessCustomerId, reason, ZonedDateTime.now())));
    }

    public void reopen() {
        if (status != ComplianceStatus.REJECTED && status != ComplianceStatus.ACTION_REQUIRED)
            throw new StepOutOfOrderException("Only rejected or action-required compliance can be reopened");
        status = ComplianceStatus.IN_PROGRESS; rejectionReason = null; failureCode = null; touch();
    }

    private void actionRequired(String reason) {
        anchorVerificationStatus = AnchorVerificationStatus.AWAITING_DOCUMENTS;
        status = ComplianceStatus.ACTION_REQUIRED; rejectionReason = reason;
        stepProgress.put(ComplianceStep.COMPLIANCE_DOCUMENTS, StepStatus.ACTION_REQUIRED);
        currentStep = ComplianceStep.COMPLIANCE_DOCUMENTS; touch();
        registerEvent(new ComplianceActionRequiredEvent(UUID.randomUUID().toString(), id, ZonedDateTime.now(),
                CorrelationId.getOrCreate(), new ComplianceActionRequiredEvent.Payload(organizationId, reason)));
    }

    private void refreshDocumentStep() {
        if (documentRequirements.stream().anyMatch(requirement -> requirement.isRequired() && !requirement.isSatisfied())) {
            stepProgress.put(ComplianceStep.COMPLIANCE_DOCUMENTS, StepStatus.ACTION_REQUIRED);
            currentStep = ComplianceStep.COMPLIANCE_DOCUMENTS;
            if (submittedAt != null) status = ComplianceStatus.ACTION_REQUIRED;
        } else if (!documentRequirements.isEmpty()) complete(ComplianceStep.COMPLIANCE_DOCUMENTS);
        touch();
    }

    private ComplianceDocumentRequirement requirement(Long id) {
        return documentRequirements.stream().filter(item -> item.getId().equals(id)).findFirst()
                .orElseThrow(() -> new InvalidComplianceDataException("Compliance document requirement was not found"));
    }

    private ComplianceDocumentRequirement requirementByAnchorId(String anchorId) {
        return documentRequirements.stream().filter(item -> Objects.equals(item.getAnchorDocumentId(), anchorId)).findFirst()
                .orElseThrow(() -> new InvalidComplianceDataException("Anchor document requirement was not found"));
    }

    private void ensureCanUpdate() {
        if (status == ComplianceStatus.APPROVED || status == ComplianceStatus.SUSPENDED)
            throw new ComplianceAlreadyApprovedException("Approved compliance cannot be edited");
        if (status == ComplianceStatus.SUBMITTED || status == ComplianceStatus.UNDER_REVIEW)
            throw new ComplianceAlreadySubmittedException("Compliance is under provider review");
    }

    private void requireComplete(ComplianceStep step) {
        if (stepProgress.get(step) != StepStatus.COMPLETE)
            throw new StepOutOfOrderException("Step " + step + " must be complete first");
    }

    private void complete(ComplianceStep step) {
        boolean changed = stepProgress.put(step, StepStatus.COMPLETE) != StepStatus.COMPLETE;
        currentStep = nextIncompleteStep().orElse(step);
        if (status == ComplianceStatus.NOT_STARTED) status = ComplianceStatus.IN_PROGRESS;
        touch();
        if (changed) registerEvent(new ComplianceStepCompletedEvent(UUID.randomUUID().toString(), id,
                ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new ComplianceStepCompletedEvent.Payload(organizationId, step.name())));
    }

    private Optional<ComplianceStep> nextIncompleteStep() {
        return Arrays.stream(ComplianceStep.values()).filter(step -> stepProgress.get(step) != StepStatus.COMPLETE).findFirst();
    }

    private static EnumMap<ComplianceStep, StepStatus> defaultProgress() {
        EnumMap<ComplianceStep, StepStatus> progress = new EnumMap<>(ComplianceStep.class);
        Arrays.stream(ComplianceStep.values()).forEach(step -> progress.put(step, StepStatus.NOT_STARTED));
        return progress;
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new InvalidComplianceDataException(name + " is required");
    }

    private void touch() { updatedAt = ZonedDateTime.now(); }
    @Override public Long getId() { return id; }
}
