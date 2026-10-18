package com.atlashub.support.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import com.atlashub.support.domain.valueobject.AuthorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "support_ticket_messages",
        indexes = {
                @Index(name = "idx_msg_ticket_created", columnList = "ticket_id, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TicketMessageJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false, length = 16)
    private AuthorType authorType;

    @Column(name = "message", nullable = false, length = 4096)
    private String message;

    @Column(name = "is_internal_note", nullable = false)
    private boolean isInternalNote;

    @Column(name = "attachment_urls_json", columnDefinition = "json")
    private String attachmentUrlsJson;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    @Column(name = "version")
    private Long version;
}
