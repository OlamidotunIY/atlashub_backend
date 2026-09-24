package com.atlashub.storage.domain.ports;

/**
 * Domain port for cloud object storage operations.
 * No Spring annotations — declared as a bean via ApplicationConfig if needed,
 * or implemented directly by an infrastructure adapter annotated @Component.
 */
public interface StoragePort {

    /**
     * Generates a short-lived pre-signed PUT URL that the frontend can use
     * to upload a file directly to Firebase Storage without proxying bytes
     * through this server.
     *
     * @param objectPath  GCS object path, e.g. "users/42/avatars/logo.png"
     * @param contentType MIME type, e.g. "image/png"
     * @param ttlMinutes  how many minutes the URL stays valid (max 7 days for V4)
     * @return the signed URL as a string
     */
    String generateSignedUploadUrl(String objectPath, String contentType, int ttlMinutes);
}
