package com.atlashub.compliance.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ServiceAgreementRequest(@NotBlank String termsVersion) {
}
