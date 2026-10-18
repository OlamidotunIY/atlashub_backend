package com.atlashub.support.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.support.domain.entities.TicketSlaConfig;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import java.util.Optional;

public interface TicketSlaConfigRepository extends Repository<TicketSlaConfig> {

    Optional<TicketSlaConfig> findByCategoryAndPriority(TicketCategory category, TicketPriority priority);
}
