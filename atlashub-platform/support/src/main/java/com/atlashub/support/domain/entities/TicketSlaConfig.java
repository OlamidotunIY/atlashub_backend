package com.atlashub.support.domain.entities;

import com.atlashub.support.domain.exceptions.SupportInvariantException;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import lombok.Getter;

@Getter
public class TicketSlaConfig {

    private final Long id;
    private final TicketCategory category;
    private final TicketPriority priority;
    private final int firstResponseMinutes;
    private final int resolutionMinutes;

    public TicketSlaConfig(
            Long id,
            TicketCategory category,
            TicketPriority priority,
            int firstResponseMinutes,
            int resolutionMinutes
    ) {
        this.id = id;
        this.category = category;
        this.priority = priority;
        this.firstResponseMinutes = firstResponseMinutes;
        this.resolutionMinutes = resolutionMinutes;
    }

    public static TicketSlaConfig create(
            Long id,
            TicketCategory category,
            TicketPriority priority,
            int firstResponseMinutes,
            int resolutionMinutes
    ) {
        if (category == null) {
            throw new SupportInvariantException("Ticket category is required for SLA config");
        }
        if (priority == null) {
            throw new SupportInvariantException("Ticket priority is required for SLA config");
        }
        if (firstResponseMinutes <= 0) {
            throw new SupportInvariantException("First response SLA minutes must be positive");
        }
        if (resolutionMinutes <= 0) {
            throw new SupportInvariantException("Resolution SLA minutes must be positive");
        }

        return new TicketSlaConfig(id, category, priority, firstResponseMinutes, resolutionMinutes);
    }
}
