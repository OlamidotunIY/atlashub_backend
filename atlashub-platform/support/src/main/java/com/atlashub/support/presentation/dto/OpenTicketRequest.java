package com.atlashub.support.presentation.dto;

import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpenTicketRequest(
        @NotBlank(message = "Subject is required")
        @Size(max = 255, message = "Subject must not exceed 255 characters")
        String subject,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Category is required")
        TicketCategory category,

        TicketPriority priority,

        String relatedEntityType,

        String relatedEntityId
) {}
