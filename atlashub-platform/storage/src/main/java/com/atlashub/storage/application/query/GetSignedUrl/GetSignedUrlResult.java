package com.atlashub.storage.application.query.GetSignedUrl;

/**
 * Result produced by {@link GetSignedUrlHandler}.
 *
 * @param signedUrl  the pre-signed PUT URL the frontend should upload to
 * @param objectPath the final GCS object path that the file will be stored at
 */
public record GetSignedUrlResult(String signedUrl, String objectPath) {}
