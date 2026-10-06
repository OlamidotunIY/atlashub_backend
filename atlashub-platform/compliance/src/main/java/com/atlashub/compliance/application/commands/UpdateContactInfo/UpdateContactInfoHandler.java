package com.atlashub.compliance.application.commands.UpdateContactInfo;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;
import com.atlashub.shared.application.port.OrganizationQueryPort;
import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;

@Component
public class UpdateContactInfoHandler extends Command<UpdateContactInfoCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(UpdateContactInfoHandler.class);

    private final ComplianceRecordRepository repository;
    private final OrganizationQueryPort organizationQueryPort;

    public UpdateContactInfoHandler(ComplianceRecordRepository repository, OrganizationQueryPort organizationQueryPort) {
        this.repository = repository;
        this.organizationQueryPort = organizationQueryPort;
    }

    @Override
    @PreAuthorize("hasAuthority('compliance:manage')")
    public ComplianceRecord execute(UpdateContactInfoCommand input) {
        log.info("Executing UpdateContactInfoCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        String country = organizationQueryPort.getCountry(input.organizationId());
        if (!country.equalsIgnoreCase(input.data().mainAddress().country())
                || !country.equalsIgnoreCase(input.data().registeredAddress().country()))
            throw new InvalidComplianceDataException("Compliance addresses must be in the organization's country");
        record.updateContactAndAddresses(input.data());
        return repository.save(record);
    }
}
