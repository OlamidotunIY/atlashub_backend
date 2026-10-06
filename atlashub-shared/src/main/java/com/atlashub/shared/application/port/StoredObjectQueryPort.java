package com.atlashub.shared.application.port;

public interface StoredObjectQueryPort {
    StoredObject readPrivateObject(String objectKey);
    record StoredObject(String objectKey, String contentType, byte[] bytes) {
        public StoredObject { bytes = bytes == null ? new byte[0] : bytes.clone(); }
        @Override public byte[] bytes() { return bytes.clone(); }
    }
}
