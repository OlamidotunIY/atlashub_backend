package com.atlashub.authentication.domain.services;

import com.atlashub.authentication.domain.exceptions.InvalidPkceChallengeException;
import com.atlashub.authentication.domain.valueobject.CodeChallengeMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PkceVerifierTest {

    private PkceVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new PkceVerifier();
    }

    @Test
    void verifies_rfc_7636_test_vector_successfully() {
        String codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

        assertDoesNotThrow(() -> verifier.verify(codeVerifier, codeChallenge, CodeChallengeMethod.S256));
    }

    @Test
    void rejects_mismatched_verifier() {
        String codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk-altered";
        String codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

        assertThrows(InvalidPkceChallengeException.class, () ->
                verifier.verify(codeVerifier, codeChallenge, CodeChallengeMethod.S256));
    }

    @Test
    void rejects_short_verifier() {
        String shortVerifier = "too_short_verifier_12345";
        String codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

        assertThrows(InvalidPkceChallengeException.class, () ->
                verifier.verify(shortVerifier, codeChallenge, CodeChallengeMethod.S256));
    }

    @Test
    void rejects_verifier_with_illegal_characters() {
        String illegalVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk!@#$";
        String codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

        assertThrows(InvalidPkceChallengeException.class, () ->
                verifier.verify(illegalVerifier, codeChallenge, CodeChallengeMethod.S256));
    }

    @Test
    void rejects_null_or_blank_inputs() {
        assertThrows(InvalidPkceChallengeException.class, () ->
                verifier.verify(null, "challenge", CodeChallengeMethod.S256));

        assertThrows(InvalidPkceChallengeException.class, () ->
                verifier.verify("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk", null, CodeChallengeMethod.S256));
    }
}
