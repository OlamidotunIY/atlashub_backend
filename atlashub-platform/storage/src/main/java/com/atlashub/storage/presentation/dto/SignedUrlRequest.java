package com.atlashub.storage.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Request parameters for {@code GET /api/v1/storage/signed-url}.
 *
 * @param objectPath  relative path within the user's storage namespace,
 *                    e.g. {@code "avatars/logo.png"} or {@code "documents/kyc.pdf"}.
 *                    Must not start with a slash or contain {@code ..} segments.
 * @param contentType MIME type the frontend will include in the {@code Content-Type}
 *                    header when it PUTs the file, e.g. {@code "image/png"}.
 * @param ttlMinutes  optional URL validity window in minutes (1–60).
 *                    Defaults to 15 in the handler when 0 is passed.
 */
public record SignedUrlRequest(
        @NotBlank(message = "objectPath is required")
        @Pattern(
                regexp = "^(?!/)(?!.*\\.\\.).*[^/]$",
                message = "objectPath must be a relative path with no leading slash or '..' segments"
        )
        String objectPath,

        @NotBlank(message = "contentType is required")
        String contentType,

        @Positive(message = "ttlMinutes must be a positive number")
        int ttlMinutes
) {}
