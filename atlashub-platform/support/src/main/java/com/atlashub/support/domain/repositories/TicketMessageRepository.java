package com.atlashub.support.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.support.domain.entities.TicketMessage;
import java.util.List;

public interface TicketMessageRepository extends Repository<TicketMessage> {

    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
