package com.atlashub.compliance.application.commands.AcceptServiceAgreement;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.ZonedDateTime;

@Component
public class AcceptServiceAgreementHandler extends Command<AcceptServiceAgreementCommand, ComplianceRecord> {

    private static final Logger log = LoggerFactory.getLogger(AcceptServiceAgreementHandler.class);

    private final ComplianceRecordRepository repository;

    public AcceptServiceAgreementHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComplianceRecord execute(AcceptServiceAgreementCommand input) {
        log.info("Executing AcceptServiceAgreementCommand for organizationId: {}", input.organizationId());

        ComplianceRecord record = repository.findByOrganizationId(input.organizationId())
            .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + input.organizationId()));

        record.acceptServiceAgreement(input.ipAddress(), ZonedDateTime.now(), input.termsVersion());
        return repository.save(record);
    }
}
