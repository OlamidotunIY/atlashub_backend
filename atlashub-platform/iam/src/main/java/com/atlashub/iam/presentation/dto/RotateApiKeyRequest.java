package com.atlashub.iam.presentation.dto;

import jakarta.validation.constraints.Size;

public record RotateApiKeyRequest(@Size(max = 120) String name) {}
