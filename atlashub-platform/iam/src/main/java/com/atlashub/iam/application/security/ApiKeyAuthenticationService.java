package com.atlashub.iam.application.security;

import com.atlashub.iam.application.port.ApiSecretProtector;
import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ApiKeyAuthenticationService {
    private final ApiKeyRepository apiKeyRepository;
    private final CustomRoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ApiSecretProtector secretProtector;

    public ApiKeyAuthenticationService(ApiKeyRepository apiKeyRepository,
                                       CustomRoleRepository roleRepository,
                                       PermissionRepository permissionRepository,
                                       ApiSecretProtector secretProtector) {
        this.apiKeyRepository = apiKeyRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.secretProtector = secretProtector;
    }

    public Optional<AuthenticatedApiKey> authenticate(String publicKey, String canonicalMessage,
                                                       String suppliedSignature) {
        ApiKey apiKey = apiKeyRepository.findByPublicKey(publicKey)
                .filter(key -> !Boolean.TRUE.equals(key.getRevoked()))
                .orElse(null);
        if (apiKey == null) return Optional.empty();
        String secret = secretProtector.decrypt(apiKey.getSecretKeyCiphertext());
        byte[] expected = hmac(secret, canonicalMessage);
        byte[] supplied;
        try {
            supplied = HexFormat.of().parseHex(suppliedSignature);
        } catch (IllegalArgumentException error) {
            return Optional.empty();
        }
        if (!MessageDigest.isEqual(expected, supplied)) return Optional.empty();

        CustomRole role = roleRepository.findById(apiKey.getBoundRoleId()).orElse(null);
        if (role == null || !role.getOrganizationId().equals(apiKey.getOrganizationId())) return Optional.empty();
        Set<String> permissions = role.isBuiltIn()
                ? permissionRepository.findAllByActiveTrue().stream().map(permission -> permission.getCode()).collect(Collectors.toSet())
                : permissionRepository.findAllById(role.getPermissions()).stream()
                        .filter(permission -> permission.isActive()).map(permission -> permission.getCode()).collect(Collectors.toSet());
        apiKey.recordUsage();
        apiKeyRepository.save(apiKey);
        return Optional.of(new AuthenticatedApiKey(apiKey.getOrganizationId(), apiKey.getEnvironment().name(),
                publicKey, permissions));
    }

    private byte[] hmac(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
            throw new IllegalStateException("Unable to verify API signature", error);
        }
    }

    public record AuthenticatedApiKey(Long organizationId, String environment,
                                      String publicKey, Set<String> permissions) {}
}
