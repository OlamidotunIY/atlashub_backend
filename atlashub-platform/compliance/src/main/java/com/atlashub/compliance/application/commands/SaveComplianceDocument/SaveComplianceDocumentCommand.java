package com.atlashub.compliance.application.commands.SaveComplianceDocument;
public record SaveComplianceDocumentCommand(Long organizationId, Long requirementId, String storageObjectKey, String textValue) {}
