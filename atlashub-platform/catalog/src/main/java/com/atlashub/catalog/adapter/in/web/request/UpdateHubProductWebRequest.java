package com.atlashub.catalog.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateHubProductWebRequest(
        @NotBlank String name,
        @NotBlank String description
) {
}
