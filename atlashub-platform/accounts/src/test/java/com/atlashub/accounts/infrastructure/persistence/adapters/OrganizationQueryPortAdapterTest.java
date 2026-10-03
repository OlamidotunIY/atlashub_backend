package com.atlashub.accounts.infrastructure.persistence.adapters;

import com.atlashub.accounts.infrastructure.persistence.entities.OrganizationJPA;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataOrganizationRepository;
import com.atlashub.shared.application.port.OrganizationQueryPort.OrganizationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganizationQueryPortAdapterTest {

    @Test
    void maps_the_complete_atlashub_registration_identity() {
        SpringDataOrganizationRepository repository = mock(SpringDataOrganizationRepository.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        LocalDate registrationDate = LocalDate.of(2026, 10, 3);
        ZonedDateTime now = ZonedDateTime.now();

        when(redis.opsForValue()).thenReturn(values);
        when(values.get(OrganizationQueryPortAdapter.ORGANIZATION_BY_ID_KEY_PREFIX + 20L)).thenReturn(null);
        when(repository.findById(20L)).thenReturn(Optional.of(new OrganizationJPA(
                20L, "Tolu Store", "SOLE_PROPRIETORSHIP", "RETAIL", registrationDate,
                null, "NGN", null, null, "NG", now, now, 0L)));

        OrganizationQueryPortAdapter adapter = new OrganizationQueryPortAdapter(
                repository, redis, objectMapper, Duration.ofHours(12));

        OrganizationDto result = adapter.findById(20L).orElseThrow();

        assertEquals(registrationDate, result.registrationDate());
        assertEquals("SOLE_PROPRIETORSHIP", result.registrationType());
        assertEquals("RETAIL", result.industry());
    }
}
