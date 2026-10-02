package com.atlashub.compliance.application.commands.UpdateComplianceDocuments;

import com.atlashub.compliance.domain.valueobject.ComplianceDocumentsData;

public record UpdateComplianceDocumentsCommand(Long organizationId, ComplianceDocumentsData data) {
}
