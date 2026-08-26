package com.atlashub.identity.domain.model;

import com.atlashub.identity.domain.event.*;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.exception.BusinessRuleException;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Organization extends AggregateRoot<Long> {

    private final Long id;
    private final Country country;
    private String businessName;
    private String firstName;
    private String lastName;
    private final EmailAddress email;
    private PhoneNumber phone;
    
    private final BusinessType businessType;
    private ComplianceStatus complianceStatus;
    private ComplianceStep complianceStep;
    private OrganizationCompliance compliance;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Organization(Long id, Country country, String businessName, String firstName, String lastName,
                    EmailAddress email, PhoneNumber phone, BusinessType businessType) {
        
        if (country != Country.NIGERIA) {
            throw new BusinessRuleException(IdentityErrorCode.UNSUPPORTED_COUNTRY, "Currently, only Organizations in Nigeria (NG) are supported.");
        }

        this.id = id;
        this.country = country;
        this.businessName = businessName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        
        this.businessType = businessType;
        
        this.complianceStatus = ComplianceStatus.NOT_STARTED;
        this.complianceStep = null;
        this.compliance = new OrganizationCompliance();
        
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = this.createdAt;

        registerEvent(new OrganizationRegistered(
            UUID.randomUUID().toString(),
            String.valueOf(id),
            ZonedDateTime.now(),
            new OrganizationRegistered.Payload(
                this.businessName,
                this.email.value(),
                this.country.name(),
                this.businessType
            )
        ));
    }

    // Reconstitution constructor for Mappers
    public Organization(Long id, Country country, String businessName, String firstName, String lastName,
                    EmailAddress email, PhoneNumber phone, BusinessType businessType,
                    ComplianceStatus complianceStatus, ComplianceStep complianceStep,
                    ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.country = country;
        this.businessName = businessName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        
        this.businessType = businessType;
        this.complianceStatus = complianceStatus;
        this.complianceStep = complianceStep;
        this.compliance = new OrganizationCompliance();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public void completeComplianceStep(ComplianceStep step) {
        if (this.complianceStatus == ComplianceStatus.NOT_STARTED) {
            this.complianceStatus = ComplianceStatus.IN_PROGRESS;
        }
        
        if (this.complianceStep == null && step != ComplianceStep.PROFILE) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_STEP_OUT_OF_ORDER, "First step must be PROFILE");
        }
        
        if (this.complianceStep != null && step != this.complianceStep.next() && step != this.complianceStep) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_STEP_OUT_OF_ORDER, "Steps must be completed in order");
        }
        
        this.complianceStep = step;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new OrganizationComplianceStepCompleted(
            UUID.randomUUID().toString(),
            String.valueOf(id),
            ZonedDateTime.now(),
            new OrganizationComplianceStepCompleted.Payload(step)
        ));
    }

    public void updateComplianceProfile(String description, StaffSize staffSize, String industry, String category, java.math.BigDecimal annualProjectedSalesVolume, String annualProjectedSalesCurrency) {
        this.compliance.updateProfileStep(description, staffSize, industry, category, annualProjectedSalesVolume, annualProjectedSalesCurrency);
        this.completeComplianceStep(ComplianceStep.PROFILE);
    }

    public void updateComplianceContact(EmailAddress supportEmail, EmailAddress disputeEmail, PhoneNumber whatsappPhone, String whatsappName, String websiteUrl, String twitterHandle, String facebookUsername, String instagramHandle, String businessState, String businessLga, String businessCity, String businessStreet) {
        this.compliance.updateContactStep(supportEmail, disputeEmail, whatsappPhone, whatsappName, websiteUrl, twitterHandle, facebookUsername, instagramHandle, businessState, businessLga, businessCity, businessStreet);
        this.completeComplianceStep(ComplianceStep.CONTACT);
    }

    public void updateComplianceOwner(String ownerBvn, String ownerNin, java.time.LocalDate ownerDateOfBirth, String ownerAddress, GovernmentIdType ownerIdType, String ownerIdNumber, String rcNumber) {
        this.compliance.updateOwnerStep(ownerBvn, ownerNin, ownerDateOfBirth, ownerAddress, ownerIdType, ownerIdNumber, rcNumber);
        this.completeComplianceStep(ComplianceStep.OWNER);
    }

    public void updateComplianceAccount(String settlementBankCode, String settlementAccountNumber, String settlementAccountName) {
        this.compliance.updateAccountStep(settlementBankCode, settlementAccountNumber, settlementAccountName);
        this.completeComplianceStep(ComplianceStep.ACCOUNT);
    }

    public void acceptServiceAgreement() {
        this.compliance.acceptServiceAgreement(ZonedDateTime.now());
        this.completeComplianceStep(ComplianceStep.SERVICE_AGREEMENT);
    }

    public void submitCompliance() {
        if (this.complianceStep != ComplianceStep.SERVICE_AGREEMENT || !this.compliance.isAgreedToTerms()) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_NOT_ALL_STEPS_COMPLETE, "All 5 compliance steps must be completed before submission");
        }
        
        this.complianceStatus = ComplianceStatus.SUBMITTED;
        this.updatedAt = ZonedDateTime.now();
        
        registerEvent(new OrganizationComplianceSubmitted(
            UUID.randomUUID().toString(),
            String.valueOf(id),
            ZonedDateTime.now()
        ));
    }

    public void approveCompliance() {
        if (this.complianceStatus != ComplianceStatus.SUBMITTED && this.complianceStatus != ComplianceStatus.UNDER_REVIEW) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_NOT_SUBMITTED, "Cannot approve compliance that hasn't been submitted");
        }
        
        this.complianceStatus = ComplianceStatus.APPROVED;
        this.updatedAt = ZonedDateTime.now();
        
        registerEvent(new OrganizationComplianceApproved(
            UUID.randomUUID().toString(),
            String.valueOf(id),
            ZonedDateTime.now(),
            new OrganizationComplianceApproved.Payload(this.firstName + " " + this.lastName, this.country)
        ));
    }

    public void rejectCompliance(String reason) {
        if (this.complianceStatus != ComplianceStatus.SUBMITTED && this.complianceStatus != ComplianceStatus.UNDER_REVIEW) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_NOT_SUBMITTED, "Cannot reject compliance that hasn't been submitted");
        }
        
        this.complianceStatus = ComplianceStatus.REJECTED;
        this.updatedAt = ZonedDateTime.now();
        
        registerEvent(new OrganizationComplianceRejected(
            UUID.randomUUID().toString(),
            String.valueOf(id),
            ZonedDateTime.now(),
            new OrganizationComplianceRejected.Payload(reason)
        ));
    }

    public void updateProfile(String firstName, String lastName, String businessName, PhoneNumber phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.businessName = businessName;
        this.phone = phone;
        this.updatedAt = ZonedDateTime.now();
        
        registerEvent(new OrganizationProfileUpdated(
            UUID.randomUUID().toString(),
            String.valueOf(id),
            ZonedDateTime.now(),
            new OrganizationProfileUpdated.Payload(
                this.firstName,
                this.lastName,
                this.businessName,
                this.phone.value()
            )
        ));
    }
    
    public void ban(String reason) {
        if (this.complianceStatus == ComplianceStatus.REJECTED) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_NOT_SUBMITTED, "Organization is already rejected/banned");
        }
        
        // We will repurpose REJECTED for now or just emit the event
        this.complianceStatus = ComplianceStatus.REJECTED;
        this.updatedAt = ZonedDateTime.now();
        
        registerEvent(new OrganizationBanned(
            UUID.randomUUID().toString(),
            String.valueOf(id),
            ZonedDateTime.now(),
            new OrganizationBanned.Payload(reason)
        ));
    }

    @Override
    public Long getId() {
        return id;
    }

}

