package com.atlashub.iam.application.security;

import com.atlashub.iam.application.port.ApiSecretProtector;
import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.iam.domain.valueobject.PermissionAction;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ApiKeyAuthenticationServiceTest {
    @Test
    void bound_key_receives_selected_roles_active_permissions_without_synchronous_usage_write() throws Exception {
        ApiKeyRepository keys = mock(ApiKeyRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class);
        ApiSecretProtector protector = mock(ApiSecretProtector.class);
        ZonedDateTime now = ZonedDateTime.now();
        ApiKey key = new ApiKey(1L, 2L, "public", "cipher", "Default", ApiEnvironment.TEST,
                4L, false, null, null, null, now, now);
        CustomRole role = CustomRole.create(4L, 2L, "Cashier", "Cashier", Set.of(3L), false, 5L);
        Permission permission = new Permission(3L, "pay:charges:create", "pay", "charges",
                PermissionAction.CREATE, "Create charges", "Create charges", true, now, now);
        when(keys.findByPublicKey("public")).thenReturn(Optional.of(key));
        when(protector.decrypt("cipher")).thenReturn("secret");
        when(roles.findById(4L)).thenReturn(Optional.of(role));
        when(permissions.findAllById(Set.of(3L))).thenReturn(List.of(permission));
        String message = "canonical";

        var result = new ApiKeyAuthenticationService(keys, roles, permissions, protector)
                .authenticate("public", message, signature("secret", message)).orElseThrow();

        assertEquals(java.util.Set.of("pay:charges:create"), result.permissions());
        verify(keys, never()).save(any());
        verify(roles).findById(4L);
    }

    private String signature(String secret, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }
}
