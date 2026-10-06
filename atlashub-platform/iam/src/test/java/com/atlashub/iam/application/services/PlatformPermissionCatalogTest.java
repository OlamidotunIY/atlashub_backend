package com.atlashub.iam.application.services;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

        PlatformPermissionCatalog.Definition complianceRead = definitions.get("compliance:read");

        assertEquals("compliance", complianceRead.module());
        assertEquals("compliance", complianceRead.resource());
        assertEquals("READ", complianceRead.action().name());
    }
}
