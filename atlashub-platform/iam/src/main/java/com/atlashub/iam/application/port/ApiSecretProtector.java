package com.atlashub.iam.application.port;

public interface ApiSecretProtector {
    String encrypt(String secret);

    String decrypt(String ciphertext);
}
