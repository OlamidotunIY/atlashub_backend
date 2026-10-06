package com.atlashub.compliance.application.commands.ProvisionTestAnchorCustomer;

import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProvisionTestAnchorCustomerHandler extends Command<ProvisionTestAnchorCustomerCommand, Void> {
    private final ComplianceRecordRepository repository;
    private final AnchorCompliancePort anchor;

    public ProvisionTestAnchorCustomerHandler(ComplianceRecordRepository repository, AnchorCompliancePort anchor) {
        this.repository = repository;
        this.anchor = anchor;
    }

    @Override
    public Void execute(ProvisionTestAnchorCustomerCommand command) {
        ComplianceRecord record = repository.findByOrganizationId(command.organizationId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found"));
        if (record.getSandboxAnchorBusinessCustomerId() != null) return null;

        AnchorCompliancePort.BusinessCustomerResult created =
                anchor.createBusinessCustomer(request(record), ApiEnvironment.TEST);
        record.recordSandboxAnchorCustomerCreated(created.customerId());
        repository.save(record);
        return null;
    }

    private AnchorCompliancePort.BusinessCustomerRequest request(ComplianceRecord record) {
        List<AnchorCompliancePort.Officer> officers = record.getOfficers().stream().map(officer ->
                new AnchorCompliancePort.Officer(officer.getId(), officer.getRole(), officer.getFirstName(),
                        officer.getMiddleName(), officer.getLastName(), officer.getMaidenName(), officer.getNationality(),
                        officer.getDateOfBirth(), officer.getEmail().value(), officer.getPhoneNumber().value(),
                        officer.getResidentialAddress(), officer.getBvn(), officer.getTitle(), officer.getPercentageOwned())).toList();
        return new AnchorCompliancePort.BusinessCustomerRequest(record.getOrganizationId(), record.getBusinessProfile(),
                record.getContactInfo(), officers);
    }
}
