package com.atlashub.shared.infrastructure.service;

import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DomainSequenceGeneratorTest {

    @Test
    void auto_seeds_a_missing_sequence_at_one() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(
                eq("SELECT next_val FROM domain_sequences WHERE sequence_name = ? FOR UPDATE"),
                eq(Long.class), eq("new_entity_seq")))
                .thenThrow(new EmptyResultDataAccessException(1));

        DomainSequenceGenerator generator = new DomainSequenceGenerator(jdbcTemplate);

        assertEquals(1L, generator.nextIdentity("new_entity_seq"));
        verify(jdbcTemplate).update(
                "INSERT INTO domain_sequences (sequence_name, next_val) VALUES (?, ?)",
                "new_entity_seq", 1L);
        verify(jdbcTemplate).update(
                "UPDATE domain_sequences SET next_val = next_val + 1 WHERE sequence_name = ?",
                "new_entity_seq");
    }
}
