package com.atlashub.compliance.application.commands.UpdateBusinessProfile;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;
import com.atlashub.shared.application.port.OrganizationQueryPort;
import com.atlashub.compliance.domain.valueobject.BusinessProfileData;
import com.atlashub.compliance.domain.valueobject.LegalRegistrationType;
import com.atlashub.compliance.domain.valueobject.SupportedBusinessIndustry;
import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;
import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.compliance.domain.entities.ComplianceDocumentRequirement;
import com.atlashub.compliance.domain.valueobject.RequirementSource;

@Component
public class UpdateBusinessProfileHandler extends Command<UpdateBusinessProfileCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(UpdateBusinessProfileHandler.class);

    private final ComplianceRecordRepository repository;
    private final OrganizationQueryPort organizationQueryPort;
    private final AnchorCompliancePort anchorCompliancePort;

    public UpdateBusinessProfileHandler(ComplianceRecordRepository repository, OrganizationQueryPort organizationQueryPort,
                                        AnchorCompliancePort anchorCompliancePort) {
        this.repository = repository;
        this.organizationQueryPort = organizationQueryPort;
        this.anchorCompliancePort = anchorCompliancePort;
    }

    @Override
    @PreAuthorize("hasAuthority('compliance:manage')")
    public ComplianceRecord execute(UpdateBusinessProfileCommand input) {
        log.info("Executing UpdateBusinessProfileCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        var organization = organizationQueryPort.findById(input.organizationId())
                .orElseThrow(() -> new InvalidComplianceDataException("Organization was not found"));
        BusinessProfileData profile = new BusinessProfileData(
                organization.businessName(), LegalRegistrationType.valueOf(organization.registrationType()),
                organization.registrationDate(), input.businessRegistrationNumber(), input.businessBvn(),
                SupportedBusinessIndustry.valueOf(organization.industry()), input.businessDescription(), input.website());
        record.updateBusinessRegistration(profile, "NG".equalsIgnoreCase(organization.country()));
        ComplianceRecord saved = repository.save(record);
        java.util.List<AnchorCompliancePort.DocumentRequirement> requirements;
        try {
            requirements = anchorCompliancePort.previewDocumentRequirements(profile.registrationType(), profile.registrationDate());
        } catch (IllegalStateException unavailable) {
            return saved;
        }
        saved.recordRequiredDocuments(requirements.stream().map(requirement -> new ComplianceDocumentRequirement(
                repository.nextIdentity(), null, requirement.documentType(), requirement.description(),
                requirement.required(), RequirementSource.PREFLIGHT, null, null, null, null, null, null)).toList());
        saved.getDocumentRequirements().stream()
                .filter(item -> {
                    String type = item.getDocumentType().toUpperCase(java.util.Locale.ROOT);
                    return type.equals("RC_NUMBER") || (type.contains("REGISTRATION") && type.contains("NUMBER"));
                })
                .forEach(item -> saved.saveComplianceDocument(item.getId(), null, input.businessRegistrationNumber()));
        return repository.save(saved);
    }
}
