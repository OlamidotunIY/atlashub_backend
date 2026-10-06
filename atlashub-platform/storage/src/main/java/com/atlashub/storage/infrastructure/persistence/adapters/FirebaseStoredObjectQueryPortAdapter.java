package com.atlashub.storage.infrastructure.persistence.adapters;

import com.atlashub.shared.application.port.StoredObjectQueryPort;
import com.google.cloud.storage.Blob;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FirebaseStoredObjectQueryPortAdapter implements StoredObjectQueryPort {
    private final String bucketName;
    public FirebaseStoredObjectQueryPortAdapter(@Value("${firebase.storage.bucket}") String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public StoredObject readPrivateObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank() || objectKey.startsWith("/") || objectKey.contains(".."))
            throw new IllegalArgumentException("A safe storage object key is required");
        Blob blob = StorageClient.getInstance().bucket(bucketName).get(objectKey);
        if (blob == null || !blob.exists()) throw new IllegalArgumentException("Stored object was not found");
        return new StoredObject(objectKey, blob.getContentType(), blob.getContent());
    }
}
