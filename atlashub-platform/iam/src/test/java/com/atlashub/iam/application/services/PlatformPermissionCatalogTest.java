package com.atlashub.iam.application.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformPermissionCatalogTest {
    @Test
    void catalog_codes_are_unique_and_include_iam_permissions() {
        var definitions = PlatformPermissionCatalog.definitions();
        assertEquals(definitions.size(), definitions.stream().map(PlatformPermissionCatalog.Definition::code).distinct().count());
        assertTrue(definitions.stream().anyMatch(value -> value.code().equals("iam:roles:manage")));
    }
}
