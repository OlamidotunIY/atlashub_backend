package com.atlashub.commerce.catalog.infrastructure.persistence.mappers;

import com.atlashub.commerce.catalog.domain.entities.Product;
import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.domain.valueobject.ProductType;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.ProductJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductMapperTest {

    private ProductMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapperImpl();
    }

    @Test
    @DisplayName("Should map Product domain to ProductJpa correctly")
    void shouldMapDomainToPersistence() {
        ZonedDateTime now = ZonedDateTime.now();
        Product product = new Product(
                1L,
                10L,
                20L,
                "PROD-001",
                "Product One",
                "Description",
                100L,
                200L,
                300L,
                true,
                false,
                true,
                ProductStatus.ACTIVE,
                ProductType.PHYSICAL,
                now,
                now
        );

        ProductJpa jpa = mapper.toPersistence(product);

        assertNotNull(jpa);
        assertEquals(1L, jpa.getId());
        assertEquals(10L, jpa.getOrganizationId());
        assertEquals(20L, jpa.getVendorId());
        assertEquals("PROD-001", jpa.getCode());
        assertEquals("Product One", jpa.getName());
        assertEquals("Description", jpa.getDescription());
        assertEquals(100L, jpa.getCategoryId());
        assertEquals(200L, jpa.getDepartmentId());
        assertEquals(300L, jpa.getManufacturerId());
        assertTrue(jpa.isTaxable());
        assertFalse(jpa.isService());
        assertTrue(jpa.isHasVariants());
        assertEquals(ProductStatus.ACTIVE, jpa.getStatus());
        assertEquals(ProductType.PHYSICAL, jpa.getType());
        assertEquals(now, jpa.getCreatedAt());
        assertEquals(now, jpa.getUpdatedAt());
    }

    @Test
    @DisplayName("Should map ProductJpa to Product domain correctly")
    void shouldMapPersistenceToDomain() {
        ZonedDateTime now = ZonedDateTime.now();
        ProductJpa jpa = new ProductJpa(
                1L,
                10L,
                20L,
                "PROD-001",
                "Product One",
                "Description",
                100L,
                200L,
                300L,
                true,
                false,
                true,
                ProductStatus.ACTIVE,
                ProductType.PHYSICAL,
                now,
                now,
                1L
        );

        Product domain = mapper.toDomain(jpa);

        assertNotNull(domain);
        assertEquals(1L, domain.getId());
        assertEquals(10L, domain.getOrganizationId());
        assertEquals(20L, domain.getVendorId());
        assertEquals("PROD-001", domain.getCode());
        assertEquals("Product One", domain.getName());
        assertEquals(ProductStatus.ACTIVE, domain.getStatus());
        assertEquals(ProductType.PHYSICAL, domain.getType());
    }
}
