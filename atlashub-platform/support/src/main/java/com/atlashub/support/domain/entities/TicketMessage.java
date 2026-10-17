package com.atlashub.support.domain.entities;

import com.atlashub.support.domain.exceptions.SupportInvariantException;
import com.atlashub.support.domain.valueobject.AuthorType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class TicketMessage {

    private final Long id;
    private final Long ticketId;
    private final Long authorId;
    private final AuthorType authorType;
    private final String message;
    private final boolean isInternalNote;
    private final List<String> attachmentUrls;
    private final ZonedDateTime createdAt;

    public TicketMessage(
            Long id,
            Long ticketId,
            Long authorId,
            AuthorType authorType,
            String message,
            boolean isInternalNote,
            List<String> attachmentUrls,
            ZonedDateTime createdAt
    ) {
        this.id = id;
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.authorType = authorType;
        this.message = message;
        this.isInternalNote = isInternalNote;
        this.attachmentUrls = attachmentUrls != null ? new ArrayList<>(attachmentUrls) : Collections.emptyList();
        this.createdAt = createdAt != null ? createdAt : ZonedDateTime.now();
    }

    public static TicketMessage create(
            Long id,
            Long ticketId,
            Long authorId,
            AuthorType authorType,
            String message,
            boolean isInternalNote,
            List<String> attachmentUrls
    ) {
        if (ticketId == null) {
            throw new SupportInvariantException("Ticket ID is required for message");
        }
        if (authorId == null) {
            throw new SupportInvariantException("Author ID is required for message");
        }
        if (authorType == null) {
            throw new SupportInvariantException("Author type is required for message");
        }
        if (message == null || message.isBlank()) {
            throw new SupportInvariantException("Message content cannot be blank");
        }

        return new TicketMessage(
                id,
                ticketId,
                authorId,
                authorType,
                message.trim(),
                isInternalNote,
                attachmentUrls,
                ZonedDateTime.now()
        );
    }
}
