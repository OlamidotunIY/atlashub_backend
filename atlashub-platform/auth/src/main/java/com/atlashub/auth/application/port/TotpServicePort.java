package com.atlashub.auth.application.port;

public interface TotpServicePort {
    String generateSecret();
    String generateUri(String secret, String accountName);
    boolean verify(String secret, String code);
}
