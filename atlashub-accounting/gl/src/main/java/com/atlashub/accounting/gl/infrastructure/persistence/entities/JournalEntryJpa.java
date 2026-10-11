package com.atlashub.accounting.gl.infrastructure.persistence.entities;

import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "accounting_journal_entries",
        indexes = {
                @Index(name = "idx_entry_number", columnList = "entry_number", unique = true),
                @Index(name = "idx_entry_org_status", columnList = "organization_id, status"),
                @Index(name = "idx_entry_org_date", columnList = "organization_id, date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JournalEntryJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "entry_number", nullable = false, length = 64)
    private String entryNumber;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 128)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private JournalEntryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EntrySource source;

    @Column(name = "initiated_by")
    private Long initiatedBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private ZonedDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
