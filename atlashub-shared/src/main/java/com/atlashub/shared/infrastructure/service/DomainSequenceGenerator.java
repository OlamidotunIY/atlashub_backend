package com.atlashub.shared.infrastructure.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DomainSequenceGenerator {

    private final JdbcTemplate jdbcTemplate;

    public DomainSequenceGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        seedSequence("organization_seq", 1L);
        seedSequence("user_seq", 1L);
        seedSequence("hub_product_seq", 1L);
        seedSequence("product_pricing_seq", 1L);
        seedSequence("auth_account_seq", 1L);
        seedSequence("otp_verification_seq", 1L);
        seedSequence("trusted_device_seq", 1L);
    }

    private void seedSequence(String sequenceName, Long initialValue) {
        String checkSql = "SELECT COUNT(*) FROM domain_sequences WHERE sequence_name = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, sequenceName);
        if (count != null && count == 0) {
            jdbcTemplate.update("INSERT INTO domain_sequences (sequence_name, next_val) VALUES (?, ?)", sequenceName, initialValue);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long nextIdentity(String sequenceName) {
        String selectSql = "SELECT next_val FROM domain_sequences WHERE sequence_name = ? FOR UPDATE";
        Long currentVal;
        try {
            currentVal = jdbcTemplate.queryForObject(selectSql, Long.class, sequenceName);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // Auto-seed if it doesn't exist
            jdbcTemplate.update("INSERT INTO domain_sequences (sequence_name, next_val) VALUES (?, ?)", sequenceName, 1000L);
            currentVal = 1000L;
        }
        
        if (currentVal == null) {
            throw new IllegalStateException("Sequence not found: " + sequenceName);
        }

        String updateSql = "UPDATE domain_sequences SET next_val = next_val + 1 WHERE sequence_name = ?";
        jdbcTemplate.update(updateSql, sequenceName);

        return currentVal;
    }
}
