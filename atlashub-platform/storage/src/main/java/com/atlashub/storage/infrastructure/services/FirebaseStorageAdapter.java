package com.atlashub.storage.infrastructure.services;

import com.atlashub.storage.domain.ports.StoragePort;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Infrastructure adapter that implements {@link StoragePort} using the
 * Firebase Admin SDK and Google Cloud Storage V4 signed URLs.
 *
 * <p>The service account configured in {@link com.google.firebase.FirebaseApp}
 * (initialised in ApplicationConfig) must have the
 * {@code roles/storage.objectAdmin} IAM role on the GCS bucket, or the
 * {@code iam.serviceAccounts.signBlob} permission, for signing to succeed.
 */
@Component
public class FirebaseStorageAdapter implements StoragePort {

    private final String bucketName;

    public FirebaseStorageAdapter(@Value("${firebase.storage.bucket}") String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public String generateSignedUploadUrl(String objectPath, String contentType, int ttlMinutes) {
        Storage storage = StorageClient.getInstance().bucket(bucketName).getStorage();

        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectPath))
                .setContentType(contentType)
                .build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                ttlMinutes, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature()
        );

        return signedUrl.toString();
    }
}
