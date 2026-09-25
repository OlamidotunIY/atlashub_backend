package com.atlashub.catalog.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateHubProductWebRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 1000) String description
) {
}
