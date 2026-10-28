package com.atlashub.integrations.application.port;

public interface CredentialVaultPort {
    String store(String credential);
    String read(String reference);
    default void delete(String reference) {}
}
