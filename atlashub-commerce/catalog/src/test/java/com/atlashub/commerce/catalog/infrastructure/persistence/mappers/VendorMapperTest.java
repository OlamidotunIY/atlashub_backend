package com.atlashub.commerce.catalog.infrastructure.persistence.mappers;

import com.atlashub.commerce.catalog.domain.entities.Vendor;
import com.atlashub.commerce.catalog.domain.valueobject.DisbursementSchedule;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.VendorJpa;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VendorMapperTest {

    private VendorMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new VendorMapperImpl();
        ReflectionTestUtils.setField(mapper, "valueObjectMapper", new ValueObjectMapper());
    }

    @Test
    @DisplayName("Should map Vendor domain to VendorJpa correctly")
    void shouldMapDomainToPersistence() {
        ZonedDateTime now = ZonedDateTime.now();
        Vendor vendor = new Vendor(
                1L,
                10L,
                20L,
                "Vendor Corp",
                new EmailAddress("vendor@corp.com"),
                new PhoneNumber("+2348099887766"),
                "044",
                "0123456789",
                "Vendor Corp Ltd",
                new BigDecimal("0.05"),
                DisbursementSchedule.WEEKLY,
                VendorStatus.ACTIVE,
                now,
                now
        );

        VendorJpa jpa = mapper.toPersistence(vendor);

        assertNotNull(jpa);
        assertEquals(1L, jpa.getId());
        assertEquals(10L, jpa.getOrganizationId());
        assertEquals(20L, jpa.getUserId());
        assertEquals("Vendor Corp", jpa.getBusinessName());
        assertEquals("vendor@corp.com", jpa.getEmail());
        assertEquals("+2348099887766", jpa.getPhone());
        assertEquals("044", jpa.getSettlementBankCode());
        assertEquals("0123456789", jpa.getSettlementAccountNumber());
        assertEquals("Vendor Corp Ltd", jpa.getSettlementAccountName());
        assertEquals(new BigDecimal("0.05"), jpa.getCommissionRate());
        assertEquals(DisbursementSchedule.WEEKLY, jpa.getDisbursementSchedule());
        assertEquals(VendorStatus.ACTIVE, jpa.getStatus());
    }

    @Test
    @DisplayName("Should map VendorJpa to Vendor domain correctly")
    void shouldMapPersistenceToDomain() {
        ZonedDateTime now = ZonedDateTime.now();
        VendorJpa jpa = new VendorJpa(
                1L,
                10L,
                20L,
                "Vendor Corp",
                "vendor@corp.com",
                "+2348099887766",
                "044",
                "0123456789",
                "Vendor Corp Ltd",
                new BigDecimal("0.05"),
                DisbursementSchedule.WEEKLY,
                VendorStatus.ACTIVE,
                now,
                now,
                1L
        );

        Vendor domain = mapper.toDomain(jpa);

        assertNotNull(domain);
        assertEquals(1L, domain.getId());
        assertEquals(10L, domain.getOrganizationId());
        assertEquals(20L, domain.getUserId());
        assertEquals("Vendor Corp", domain.getBusinessName());
        assertNotNull(domain.getEmail());
        assertEquals("vendor@corp.com", domain.getEmail().value());
        assertNotNull(domain.getPhone());
        assertEquals("+2348099887766", domain.getPhone().value());
        assertEquals(VendorStatus.ACTIVE, domain.getStatus());
    }
}
