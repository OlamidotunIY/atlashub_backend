package com.atlashub.authentication.domain.services;

import com.atlashub.authentication.domain.exceptions.InvalidPkceChallengeException;
import com.atlashub.authentication.domain.valueobject.CodeChallengeMethod;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Pattern;

public class PkceVerifier {

    private static final Pattern CODE_VERIFIER_PATTERN = Pattern.compile("^[0-9a-zA-Z._~-]{43,128}$");

    public void verify(String codeVerifier, String codeChallenge, CodeChallengeMethod method) {
        if (codeVerifier == null || codeVerifier.isBlank()) {
            throw new InvalidPkceChallengeException("PKCE code_verifier is required");
        }
        if (!CODE_VERIFIER_PATTERN.matcher(codeVerifier).matches()) {
            throw new InvalidPkceChallengeException("PKCE code_verifier must be between 43 and 128 characters of [A-Za-z0-9-._~]");
        }
        if (codeChallenge == null || codeChallenge.isBlank()) {
            throw new InvalidPkceChallengeException("PKCE code_challenge is missing");
        }
        if (method != CodeChallengeMethod.S256) {
            throw new InvalidPkceChallengeException("Unsupported PKCE code_challenge_method: only S256 is supported");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            String computedChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            byte[] expectedBytes = codeChallenge.getBytes(StandardCharsets.US_ASCII);
            byte[] actualBytes = computedChallenge.getBytes(StandardCharsets.US_ASCII);

            if (!MessageDigest.isEqual(expectedBytes, actualBytes)) {
                throw new InvalidPkceChallengeException("PKCE code_verifier does not match code_challenge");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
