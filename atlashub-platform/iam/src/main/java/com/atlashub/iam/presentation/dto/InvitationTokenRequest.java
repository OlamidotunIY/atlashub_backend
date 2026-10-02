package com.atlashub.iam.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record InvitationTokenRequest(@NotBlank String token) {
}
