package com.atlashub.support.infrastructure.persistence.repositories;

import com.atlashub.support.infrastructure.persistence.entities.TicketMessageJpa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTicketMessageRepository extends JpaRepository<TicketMessageJpa, Long> {

    List<TicketMessageJpa> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
