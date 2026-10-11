package com.atlashub.accounts.infrastructure.persistence.mappers;

import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.accounts.infrastructure.persistence.entities.OrganizationJPA;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrganizationMapperTest {

    private OrganizationMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrganizationMapperImpl();
        ReflectionTestUtils.setField(mapper, "valueObjectMapper", new ValueObjectMapper());
    }

    @Test
    @DisplayName("Should update fields on existing OrganizationJPA record")
    void shouldUpdatePersistenceFieldsOnExistingRecord() {
        ZonedDateTime now = ZonedDateTime.now();
        LocalDate regDate = LocalDate.now();

        OrganizationJPA existingRecord = new OrganizationJPA(
                10L, "Old Name", "SOLE_PROPRIETORSHIP", "HOTELS", regDate,
                "Old Description", "NGN", null, null, "NG",
                now.minusDays(5), now.minusDays(5), 0L
        );

        Organization domain = new Organization(
                10L, "New Name", AtlasHubRegistrationType.PRIVATE_LIMITED_COMPANY, SupportedIndustry.RETAIL, regDate,
                "New Description", CurrencyCode.NGN, "https://logo.png", "https://website.com",
                new Country("NG"), now.minusDays(5), now
        );

        mapper.updatePersistence(domain, existingRecord);

        assertEquals("New Name", existingRecord.getBusinessName());
        assertEquals("PRIVATE_LIMITED_COMPANY", existingRecord.getRegistrationType());
        assertEquals("RETAIL", existingRecord.getIndustry());
        assertEquals("New Description", existingRecord.getDescription());
        assertEquals("NGN", existingRecord.getBaseCurrency());
        assertEquals("https://logo.png", existingRecord.getLogoUrl());
        assertEquals("https://website.com", existingRecord.getWebsiteUrl());
        assertEquals("NG", existingRecord.getCountry());
    }

    @Test
    @DisplayName("Should map domain Organization to OrganizationJPA")
    void shouldMapDomainToPersistence() {
        ZonedDateTime now = ZonedDateTime.now();
        LocalDate regDate = LocalDate.now();

        Organization domain = new Organization(
                20L, "Acme Corp", AtlasHubRegistrationType.PRIVATE_LIMITED_COMPANY, SupportedIndustry.RETAIL, regDate,
                "Retail shop", CurrencyCode.NGN, null, null, new Country("NG"), now, now
        );

        OrganizationJPA jpa = mapper.toPersistence(domain);

        assertNotNull(jpa);
        assertEquals(20L, jpa.getId());
        assertEquals("Acme Corp", jpa.getBusinessName());
        assertEquals("PRIVATE_LIMITED_COMPANY", jpa.getRegistrationType());
        assertEquals("RETAIL", jpa.getIndustry());
    }

    @Test
    @DisplayName("Should map OrganizationJPA to domain Organization")
    void shouldMapPersistenceToDomain() {
        ZonedDateTime now = ZonedDateTime.now();
        LocalDate regDate = LocalDate.now();

        OrganizationJPA jpa = new OrganizationJPA(
                30L, "Star Inc", "PRIVATE_LIMITED_COMPANY", "HOTELS", regDate,
                "Hotel", "NGN", null, null, "NG", now, now, 0L
        );

        Organization domain = mapper.toDomain(jpa);

        assertNotNull(domain);
        assertEquals(30L, domain.getId());
        assertEquals("Star Inc", domain.getBusinessName());
        assertEquals(AtlasHubRegistrationType.PRIVATE_LIMITED_COMPANY, domain.getRegistrationType());
        assertEquals(SupportedIndustry.HOTELS, domain.getIndustry());
    }

    @Test
    @DisplayName("Should handle null safely")
    void shouldHandleNullSafely() {
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toPersistence(null));
    }
}
