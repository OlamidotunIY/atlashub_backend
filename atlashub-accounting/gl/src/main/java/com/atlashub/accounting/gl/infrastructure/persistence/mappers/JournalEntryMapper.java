package com.atlashub.accounting.gl.infrastructure.persistence.mappers;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.entities.JournalLine;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalEntryJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalLineJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class JournalEntryMapper implements DomainMapper<JournalEntry, JournalEntryJpa> {

    private final JournalLineMapper lineMapper;

    public JournalEntryMapper(JournalLineMapper lineMapper) {
        this.lineMapper = lineMapper;
    }

    @Override
    public JournalEntry toDomain(JournalEntryJpa record) {
        return toDomain(record, Collections.emptyList());
    }

    public JournalEntry toDomain(JournalEntryJpa record, List<JournalLineJpa> lineJpas) {
        if (record == null) {
            return null;
        }
        List<JournalLine> lines = lineJpas != null
                ? lineJpas.stream().map(lineMapper::toDomain).toList()
                : Collections.emptyList();

        return new JournalEntry(
                record.getId(),
                record.getOrganizationId(),
                record.getEntryNumber(),
                record.getDate(),
                record.getReference(),
                record.getDescription(),
                lines,
                record.getStatus(),
                record.getSource(),
                record.getInitiatedBy(),
                record.getApprovedBy(),
                record.getApprovedAt(),
                record.getCreatedAt()
        );
    }

    @Override
    public JournalEntryJpa toPersistence(JournalEntry domain) {
        if (domain == null) {
            return null;
        }
        return new JournalEntryJpa(
                domain.getId(),
                domain.getOrganizationId(),
                domain.getEntryNumber(),
                domain.getDate(),
                domain.getReference(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getSource(),
                domain.getInitiatedBy(),
                domain.getApprovedBy(),
                domain.getApprovedAt(),
                domain.getCreatedAt(),
                null
        );
    }

    @Override
    public void updatePersistence(JournalEntry domain, JournalEntryJpa record) {
        JournalEntryJpa mapped = toPersistence(domain);
        BeanUtils.copyProperties(mapped, record, "id", "version");
    }
}
