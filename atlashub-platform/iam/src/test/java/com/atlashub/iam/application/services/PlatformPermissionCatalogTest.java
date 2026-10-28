package com.atlashub.iam.application.services;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformPermissionCatalogTest {

    @Test
    void defines_only_supported_permission_actions() {
        assertDoesNotThrow(PlatformPermissionCatalog::definitions);
        assertFalse(PlatformPermissionCatalog.definitions().isEmpty());
    }

    @Test
    void maps_two_segment_permission_codes_to_their_module_resource() {
        Map<String, PlatformPermissionCatalog.Definition> definitions = PlatformPermissionCatalog.definitions().stream()
                .collect(java.util.stream.Collectors.toMap(PlatformPermissionCatalog.Definition::code, definition -> definition));

        PlatformPermissionCatalog.Definition integrationsRead = definitions.get("integrations:read");

        assertEquals("integrations", integrationsRead.module());
        assertEquals("integrations", integrationsRead.resource());
        assertEquals("READ", integrationsRead.action().name());
    }

    @Test
    void includes_the_analytics_reports_permission() {
        assertTrue(PlatformPermissionCatalog.definitions().stream()
                .anyMatch(definition -> definition.code().equals("analytics:reports:read")));
    }

    @Test
    void includes_the_event_replay_management_permission() {
        assertTrue(PlatformPermissionCatalog.definitions().stream()
                .anyMatch(definition -> definition.code().equals("eventbus:events:manage")));
    }
}
