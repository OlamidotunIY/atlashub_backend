package com.atlashub.compliance.infrastructure.persistence.mappers;

import com.atlashub.compliance.domain.entities.*;
import com.atlashub.compliance.domain.valueobject.BusinessProfileData;
import com.atlashub.compliance.infrastructure.persistence.entities.*;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ComplianceRecordMapper implements DomainMapper<ComplianceRecord, ComplianceRecordJpa> {
    @Override
    public ComplianceRecord toDomain(ComplianceRecordJpa row) {
        BusinessProfileData profile = row.getLegalName() == null ? null : new BusinessProfileData(
                row.getLegalName(), row.getRegistrationType(), row.getRegistrationDate(),
                row.getBusinessRegistrationNumber(), row.getBusinessBvn(), row.getIndustry(),
                row.getBusinessDescription(), row.getWebsite());
        return new ComplianceRecord(row.getId(), row.getOrganizationId(), row.getStatus(), row.getCurrentStep(),
                row.getStepProgress(), row.getEligibilityStatus(), row.getAnchorVerificationStatus(),
                row.getAnchorBusinessCustomerId(), row.getFailureCode(), row.getRejectionReason(),
                row.getSubmittedAt(), row.getApprovedAt(), profile, row.getContactInfo(),
                row.getOfficers().stream().map(this::toDomain).toList(),
                row.getDocumentRequirements().stream().map(this::toDomain).toList(),
                row.getServiceAgreement(), row.getCreatedAt(), row.getUpdatedAt());
    }

    @Override
    public ComplianceRecordJpa toPersistence(ComplianceRecord domain) {
        BusinessProfileData profile = domain.getBusinessProfile();
        return ComplianceRecordJpa.builder()
                .id(domain.getId()).organizationId(domain.getOrganizationId()).status(domain.getStatus())
                .currentStep(domain.getCurrentStep()).stepProgress(domain.getStepProgress())
                .eligibilityStatus(domain.getEligibilityStatus()).anchorVerificationStatus(domain.getAnchorVerificationStatus())
                .anchorBusinessCustomerId(domain.getAnchorBusinessCustomerId()).failureCode(domain.getFailureCode())
                .rejectionReason(domain.getRejectionReason()).submittedAt(domain.getSubmittedAt()).approvedAt(domain.getApprovedAt())
                .legalName(profile == null ? null : profile.legalName())
                .registrationType(profile == null ? null : profile.registrationType())
                .registrationDate(profile == null ? null : profile.registrationDate())
                .businessRegistrationNumber(profile == null ? null : profile.businessRegistrationNumber())
                .businessBvn(profile == null ? null : profile.businessBvn())
                .industry(profile == null ? null : profile.industry())
                .businessDescription(profile == null ? null : profile.businessDescription())
                .website(profile == null ? null : profile.website()).contactInfo(domain.getContactInfo())
                .serviceAgreement(domain.getServiceAgreement())
                .officers(domain.getOfficers().stream().map(this::toPersistence).toList())
                .documentRequirements(domain.getDocumentRequirements().stream().map(this::toPersistence).toList())
                .createdAt(domain.getCreatedAt()).updatedAt(domain.getUpdatedAt()).build();
    }

    private BusinessOfficer toDomain(BusinessOfficerJpa row) {
        return new BusinessOfficer(row.getId(), row.getRole(), row.getFirstName(), row.getMiddleName(),
                row.getLastName(), row.getMaidenName(), row.getNationality(), row.getDateOfBirth(),
                new EmailAddress(row.getEmail()), new PhoneNumber(row.getPhoneNumber()), row.getResidentialAddress(),
                row.getBvn(), row.getTitle(), row.getPercentageOwned(), row.getAnchorOfficerId(), row.getVerificationStatus());
    }

    private BusinessOfficerJpa toPersistence(BusinessOfficer officer) {
        return BusinessOfficerJpa.builder().id(officer.getId()).role(officer.getRole())
                .firstName(officer.getFirstName()).middleName(officer.getMiddleName()).lastName(officer.getLastName())
                .maidenName(officer.getMaidenName()).nationality(officer.getNationality()).dateOfBirth(officer.getDateOfBirth())
                .email(officer.getEmail().value()).phoneNumber(officer.getPhoneNumber().value())
                .residentialAddress(officer.getResidentialAddress()).bvn(officer.getBvn()).title(officer.getTitle())
                .percentageOwned(officer.getPercentageOwned()).anchorOfficerId(officer.getAnchorOfficerId())
                .verificationStatus(officer.getVerificationStatus()).build();
    }

    private ComplianceDocumentRequirement toDomain(ComplianceDocumentRequirementJpa row) {
        return new ComplianceDocumentRequirement(row.getId(), row.getAnchorDocumentId(), row.getDocumentType(),
                row.getDescription(), row.isRequired(), row.getSource(), row.getStatus(), row.getStorageObjectKey(),
                row.getTextValue(), row.getRejectionReason(), row.getCreatedAt(), row.getUpdatedAt());
    }

    private ComplianceDocumentRequirementJpa toPersistence(ComplianceDocumentRequirement document) {
        return ComplianceDocumentRequirementJpa.builder().id(document.getId()).anchorDocumentId(document.getAnchorDocumentId())
                .documentType(document.getDocumentType()).description(document.getDescription()).required(document.isRequired())
                .source(document.getSource()).status(document.getStatus()).storageObjectKey(document.getStorageObjectKey())
                .textValue(document.getTextValue()).rejectionReason(document.getRejectionReason())
                .createdAt(document.getCreatedAt()).updatedAt(document.getUpdatedAt()).build();
    }
}
