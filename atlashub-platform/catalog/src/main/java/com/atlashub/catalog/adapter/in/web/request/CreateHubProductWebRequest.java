package com.atlashub.catalog.adapter.in.web.request;


import com.atlashub.catalog.domain.valueobject.ProductKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateHubProductWebRequest(
        @NotNull ProductKey key,
        @NotBlank String name,
        @NotBlank String description
) {
}
