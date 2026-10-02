package com.atlashub.compliance.presentation.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record ComplianceDocumentsRequest(@NotEmpty Set<String> suppliedDocumentTypes) {
}
