package com.atlashub.shared.adapter.out.external.dlq;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Component
public class DeadLetterRepository {

    private final JdbcTemplate jdbcTemplate;

    public DeadLetterRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String id, String topic, String payload, String exceptionMessage, ZonedDateTime occurredAt) {
        String sql = "INSERT INTO dead_letter_messages (id, topic, payload, exception_message, occurred_at) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, topic, payload, exceptionMessage, Timestamp.from(occurredAt.toInstant()));
    }

    public void deleteByPayloadContaining(String eventId) {
        String sql = "DELETE FROM dead_letter_messages WHERE payload LIKE ?";
        jdbcTemplate.update(sql, "%" + eventId + "%");
    }
}
