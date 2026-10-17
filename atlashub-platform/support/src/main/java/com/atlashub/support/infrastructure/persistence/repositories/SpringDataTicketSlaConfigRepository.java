package com.atlashub.support.infrastructure.persistence.repositories;

import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.infrastructure.persistence.entities.TicketSlaConfigJpa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTicketSlaConfigRepository extends JpaRepository<TicketSlaConfigJpa, Long> {

    Optional<TicketSlaConfigJpa> findByCategoryAndPriority(TicketCategory category, TicketPriority priority);
}
