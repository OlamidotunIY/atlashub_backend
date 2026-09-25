package com.atlashub.storage.presentation.dto;

/**
 * Response payload for {@code GET /api/v1/storage/signed-url}.
 *
 * @param signedUrl  the pre-signed PUT URL — the frontend should HTTP PUT the
 *                   file bytes directly to this URL with the matching Content-Type header.
 * @param objectPath the final GCS object path (user-scoped) where the file will land.
 */
public record SignedUrlResponse(String signedUrl, String objectPath) {}
