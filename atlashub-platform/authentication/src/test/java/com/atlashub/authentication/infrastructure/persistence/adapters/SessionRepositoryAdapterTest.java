package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.infrastructure.persistence.mappers.SessionMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataSessionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionRepositoryAdapterTest {

    @Test
    void deserializes_the_redis_session_json_used_by_authentication() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        Session session = Session.create(
                13L, "token-hash", "1", 1L, "TEST", "fingerprint", "family",
                ZonedDateTime.now().plusDays(30), "127.0.0.1", "browser");

        String redisJson = objectMapper.writeValueAsString(session);
        Session restored = objectMapper.readValue(redisJson, Session.class);

        assertEquals(session.getId(), restored.getId());
        assertEquals(session.getTokenHash(), restored.getTokenHash());
        assertEquals(session.getUserId(), restored.getUserId());
        assertEquals(session.getOrganizationId(), restored.getOrganizationId());
        assertEquals(session.getEnvironment(), restored.getEnvironment());
    }

    @Test
    void invalidation_removes_only_runtime_state_and_retains_the_audit_row() throws Exception {
        SpringDataSessionRepository jpa = mock(SpringDataSessionRepository.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        @SuppressWarnings("unchecked")
        SetOperations<String, String> sets = mock(SetOperations.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        Session session = Session.create(
                10L, "hash", "2", 3L, "TEST", "fingerprint", "family",
                ZonedDateTime.now().plusDays(30), null, null);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.opsForSet()).thenReturn(sets);
        when(values.get("session:id:10")).thenReturn("json");
        when(objectMapper.readValue("json", Session.class)).thenReturn(session);

        SessionRepositoryAdapter adapter = new SessionRepositoryAdapter(
                jpa, mock(SessionMapper.class), mock(DomainSequenceGenerator.class),
                mock(DomainEventPublisher.class), redis, objectMapper);

        adapter.deleteById(10L);

        verify(redis).delete("session:hash");
        verify(redis).delete("session:id:10");
        verify(jpa, never()).deleteById(anyLong());
    }
}
