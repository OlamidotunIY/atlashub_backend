package com.atlashub.identity.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterSplitRecipientRequest(
        @NotBlank String bankCode,
        @NotBlank String accountNumber,
        String description
) {}
