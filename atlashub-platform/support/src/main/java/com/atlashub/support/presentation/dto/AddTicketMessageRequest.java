package com.atlashub.support.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AddTicketMessageRequest(
        @NotBlank(message = "Message content is required")
        String message,

        boolean isInternalNote,

        List<String> attachmentUrls
) {}
