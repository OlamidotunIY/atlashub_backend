package com.atlashub.storage.application.query.GetSignedUrl;

/**
 * Query input for generating a pre-signed Firebase Storage upload URL.
 *
 * @param userId      the authenticated user requesting the URL (used to scope the path)
 * @param objectPath  caller-supplied relative path, e.g. "avatars/logo.png"
 * @param contentType MIME type the frontend will use when uploading, e.g. "image/png"
 * @param ttlMinutes  desired URL validity window; 0 falls back to the handler default
 */
public record GetSignedUrlQuery(
        Long userId,
        String objectPath,
        String contentType,
        int ttlMinutes
) {}
