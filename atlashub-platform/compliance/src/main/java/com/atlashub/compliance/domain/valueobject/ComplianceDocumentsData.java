package com.atlashub.compliance.domain.valueobject;

import com.atlashub.shared.domain.exception.ValidationException;

import java.util.Set;

public record ComplianceDocumentsData(Set<String> suppliedDocumentTypes) {
    public ComplianceDocumentsData {
        suppliedDocumentTypes = suppliedDocumentTypes == null ? Set.of() : Set.copyOf(suppliedDocumentTypes);
        if (suppliedDocumentTypes.isEmpty()) {
            throw new ValidationException("At least one compliance document is required");
        }
    }
}
