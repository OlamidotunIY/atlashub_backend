package com.atlashub.accounts.infrastructure.persistence.mappers;

import com.atlashub.accounts.domain.entities.User;
import com.atlashub.accounts.infrastructure.persistence.entities.UserJPA;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Country;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMapperTest {

    private UserMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapperImpl();
        ReflectionTestUtils.setField(mapper, "valueObjectMapper", new ValueObjectMapper());
    }

    @Test
    @DisplayName("Should update fields on existing UserJPA record including emailVerified")
    void shouldUpdatePersistenceFieldsOnExistingRecord() {
        ZonedDateTime now = ZonedDateTime.now();
        UserJPA existingRecord = new UserJPA(
                1L, "John", "Doe", "john@example.com",
                null, null, "NG", 10L, ApiEnvironment.TEST,
                false, "en", "UTC", now.minusDays(1), now.minusDays(1), 0L
        );

        User domain = new User(
                1L, "Johnny", "Doe", new EmailAddress("john@example.com"),
                "https://img.example.com/avatar.png", new PhoneNumber("+2348012345678"),
                new Country("NG"), 20L, ApiEnvironment.LIVE, true,
                "en_US", "Africa/Lagos", now.minusDays(1), now
        );

        mapper.updatePersistence(domain, existingRecord);

        assertTrue(existingRecord.isEmailVerified());
        assertEquals("Johnny", existingRecord.getFirstName());
        assertEquals(20L, existingRecord.getActiveOrganizationId());
        assertEquals(ApiEnvironment.LIVE, existingRecord.getActiveEnvironment());
        assertEquals("https://img.example.com/avatar.png", existingRecord.getImageUrl());
        assertEquals("+2348012345678", existingRecord.getPhone());
    }

    @Test
    @DisplayName("Should map domain User to UserJPA")
    void shouldMapDomainToPersistence() {
        ZonedDateTime now = ZonedDateTime.now();
        User domain = new User(
                2L, "Jane", "Smith", new EmailAddress("jane@example.com"),
                null, null, new Country("NG"), null, ApiEnvironment.TEST, true,
                null, null, now, now
        );

        UserJPA jpa = mapper.toPersistence(domain);

        assertNotNull(jpa);
        assertEquals(2L, jpa.getId());
        assertEquals("Jane", jpa.getFirstName());
        assertEquals("Smith", jpa.getLastName());
        assertEquals("jane@example.com", jpa.getEmail());
        assertTrue(jpa.isEmailVerified());
    }

    @Test
    @DisplayName("Should map UserJPA to domain User")
    void shouldMapPersistenceToDomain() {
        ZonedDateTime now = ZonedDateTime.now();
        UserJPA jpa = new UserJPA(
                3L, "Bob", "Builder", "bob@example.com",
                null, null, "NG", 5L, ApiEnvironment.TEST,
                true, null, null, now, now, 0L
        );

        User domain = mapper.toDomain(jpa);

        assertNotNull(domain);
        assertEquals(3L, domain.getId());
        assertEquals("Bob", domain.getFirstName());
        assertEquals("bob@example.com", domain.getEmail().value());
        assertTrue(domain.isEmailVerified());
    }

    @Test
    @DisplayName("Should handle null safely")
    void shouldHandleNullSafely() {
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toPersistence(null));
    }
}
