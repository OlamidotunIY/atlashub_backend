package com.atlashub.authentication.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class RsaSigningKeyProvider {
    private static final Logger log = LoggerFactory.getLogger(RsaSigningKeyProvider.class);

    private final PrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String keyId;

    @Autowired
    public RsaSigningKeyProvider(
            @Value("${atlashub.jwt.private-key:${ATLASHUB_JWT_PRIVATE_KEY:}}") String privateKeyValue,
            @Value("${atlashub.jwt.public-key:${ATLASHUB_JWT_PUBLIC_KEY:}}") String publicKeyValue,
            @Value("${atlashub.jwt.private-key-path:${ATLASHUB_JWT_PRIVATE_KEY_PATH:}}") String privateKeyPath,
            @Value("${atlashub.jwt.public-key-path:${ATLASHUB_JWT_PUBLIC_KEY_PATH:}}") String publicKeyPath,
            @Value("${atlashub.jwt.key-id:atlashub-rs256-1}") String keyId,
            Environment environment) {
        this(privateKeyValue, publicKeyValue, privateKeyPath, publicKeyPath, keyId,
                environment.acceptsProfiles(Profiles.of("local")));
    }

    RsaSigningKeyProvider(String privateKeyValue, String publicKeyValue,
                          String privateKeyPath, String publicKeyPath, String keyId,
                          boolean allowEphemeralDevelopmentKeys) {
        this.keyId = keyId;
        KeyPair keyPair = loadOrGenerate(
                resolveKeyMaterial(privateKeyValue, privateKeyPath, "private"),
                resolveKeyMaterial(publicKeyValue, publicKeyPath, "public"),
                allowEphemeralDevelopmentKeys);
        this.privateKey = keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    public PrivateKey privateKey() {
        return privateKey;
    }

    public PublicKey publicKey() {
        return publicKey;
    }

    public String keyId() {
        return keyId;
    }

    public Map<String, Object> jwkSet() {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        Map<String, String> key = Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", "RS256",
                "kid", keyId,
                "n", encoder.encodeToString(unsigned(publicKey.getModulus().toByteArray())),
                "e", encoder.encodeToString(unsigned(publicKey.getPublicExponent().toByteArray()))
        );
        return Map.of("keys", List.of(key));
    }

    private KeyPair loadOrGenerate(String privateKeyValue, String publicKeyValue,
                                   boolean allowEphemeralDevelopmentKeys) {
        try {
            if (privateKeyValue != null && !privateKeyValue.isBlank()
                    && publicKeyValue != null && !publicKeyValue.isBlank()) {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                PrivateKey loadedPrivate = factory.generatePrivate(
                        new PKCS8EncodedKeySpec(decodePem(privateKeyValue)));
                PublicKey loadedPublic = factory.generatePublic(
                        new X509EncodedKeySpec(decodePem(publicKeyValue)));
                return new KeyPair(loadedPublic, loadedPrivate);
            }
            if (!allowEphemeralDevelopmentKeys) {
                throw new IllegalStateException(
                        "JWT RSA keys must be configured outside the explicit local profile");
            }
            log.warn("JWT RSA keys are not configured; generating an ephemeral local-development key pair");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize JWT signing keys", e);
        }
    }

    private byte[] decodePem(String value) {
        String normalized = value
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    private String resolveKeyMaterial(String inlineValue, String filePath, String keyType) {
        if (filePath == null || filePath.isBlank()) {
            return inlineValue;
        }
        try {
            return Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read JWT " + keyType + " key file: " + filePath, e);
        }
    }

    private byte[] unsigned(byte[] value) {
        if (value.length > 1 && value[0] == 0) {
            return Arrays.copyOfRange(value, 1, value.length);
        }
        return value;
    }
}
