package com.atlashub.compliance.application.commands.AcceptServiceAgreement;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Value;
import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;

@Component
public class AcceptServiceAgreementHandler extends Command<AcceptServiceAgreementCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(AcceptServiceAgreementHandler.class);

    private final ComplianceRecordRepository repository;
    private final String currentTermsVersion;

    public AcceptServiceAgreementHandler(ComplianceRecordRepository repository,
            @Value("${atlashub.compliance.terms-version}") String currentTermsVersion) {
        this.repository = repository;
        this.currentTermsVersion = currentTermsVersion;
    }

    @Override
    @PreAuthorize("hasAuthority('compliance:manage')")
    public ComplianceRecord execute(AcceptServiceAgreementCommand input) {
        log.info("Executing AcceptServiceAgreementCommand for organizationId: {}", input.organizationId());

        if (!currentTermsVersion.equals(input.termsVersion()))
            throw new InvalidComplianceDataException("The current compliance terms must be accepted");
        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        record.acceptServiceAgreement(input.ipAddress(), ZonedDateTime.now(), input.termsVersion());
        return repository.save(record);
    }
}
