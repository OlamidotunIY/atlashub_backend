package com.atlashub.catalog.adapter.in.web.request;


import jakarta.validation.constraints.NotBlank;

public record CreateHubProductWebRequest(
        @NotBlank String key,
        @NotBlank String name,
        @NotBlank String description
) {
}
