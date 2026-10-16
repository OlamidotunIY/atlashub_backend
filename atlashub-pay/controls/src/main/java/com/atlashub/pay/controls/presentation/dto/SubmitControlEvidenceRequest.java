package com.atlashub.pay.controls.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitControlEvidenceRequest(
        @NotBlank(message = "Evidence reference must not be blank")
        String evidenceReference,
        String description
) {}
