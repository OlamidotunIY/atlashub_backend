package com.atlashub.shared.application.port;

public interface StoragePort {

    String generateSignedUploadUrl(String objectPath, String contentType, int ttlMinutes);
}
