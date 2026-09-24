package com.atlashub.storage.application.query.GetSignedUrl;

import com.atlashub.storage.domain.ports.StoragePort;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

/**
 * Handles generation of a pre-signed Firebase Storage upload URL.
 *
 * <p>Business rule enforced here: the object path is always scoped under
 * {@code users/{userId}/} to prevent users from writing to arbitrary paths.
 *
 * <p>Default TTL is 15 minutes when the caller passes 0.
 */
@Component
public class GetSignedUrlHandler extends Query<GetSignedUrlQuery, GetSignedUrlResult> {

    private static final int DEFAULT_TTL_MINUTES = 15;

    private final StoragePort storagePort;

    public GetSignedUrlHandler(StoragePort storagePort) {
        this.storagePort = storagePort;
    }

    @Override
    public GetSignedUrlResult execute(GetSignedUrlQuery query) {
        int ttl = query.ttlMinutes() > 0 ? query.ttlMinutes() : DEFAULT_TTL_MINUTES;

        // Scope path to the requesting user — prevents path traversal / overwrites
        String scopedPath = "users/" + query.userId() + "/" + query.objectPath();

        String signedUrl = storagePort.generateSignedUploadUrl(scopedPath, query.contentType(), ttl);

        return new GetSignedUrlResult(signedUrl, scopedPath);
    }
}
