package com.atlashub.commerce.catalog.infrastructure.persistence.mappers;

import com.atlashub.commerce.catalog.domain.entities.Supplier;
import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.SupplierJpa;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SupplierMapperTest {

    private SupplierMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new SupplierMapperImpl();
        ReflectionTestUtils.setField(mapper, "valueObjectMapper", new ValueObjectMapper());
    }

    @Test
    @DisplayName("Should map Supplier domain to SupplierJpa correctly")
    void shouldMapDomainToPersistence() {
        ZonedDateTime now = ZonedDateTime.now();
        Supplier supplier = new Supplier(
                1L,
                10L,
                "Acme Supplies",
                new EmailAddress("info@acme.com"),
                new PhoneNumber("+2348012345678"),
                "123 Market St",
                SupplierStatus.ACTIVE,
                now,
                now
        );

        SupplierJpa jpa = mapper.toPersistence(supplier);

        assertNotNull(jpa);
        assertEquals(1L, jpa.getId());
        assertEquals(10L, jpa.getOrganizationId());
        assertEquals("Acme Supplies", jpa.getName());
        assertEquals("info@acme.com", jpa.getEmail());
        assertEquals("+2348012345678", jpa.getPhone());
        assertEquals("123 Market St", jpa.getAddress());
        assertEquals(SupplierStatus.ACTIVE, jpa.getStatus());
    }

    @Test
    @DisplayName("Should map SupplierJpa to Supplier domain correctly")
    void shouldMapPersistenceToDomain() {
        ZonedDateTime now = ZonedDateTime.now();
        SupplierJpa jpa = new SupplierJpa(
                1L,
                10L,
                "Acme Supplies",
                "info@acme.com",
                "+2348012345678",
                "123 Market St",
                SupplierStatus.ACTIVE,
                now,
                now,
                1L
        );

        Supplier domain = mapper.toDomain(jpa);

        assertNotNull(domain);
        assertEquals(1L, domain.getId());
        assertEquals(10L, domain.getOrganizationId());
        assertEquals("Acme Supplies", domain.getName());
        assertNotNull(domain.getEmail());
        assertEquals("info@acme.com", domain.getEmail().value());
        assertNotNull(domain.getPhone());
        assertEquals("+2348012345678", domain.getPhone().value());
        assertEquals(SupplierStatus.ACTIVE, domain.getStatus());
    }
}
