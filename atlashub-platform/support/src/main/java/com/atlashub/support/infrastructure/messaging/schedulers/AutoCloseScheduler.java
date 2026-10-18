package com.atlashub.support.infrastructure.messaging.schedulers;

import com.atlashub.support.application.command.CloseTicket.CloseTicketCommand;
import com.atlashub.support.application.command.CloseTicket.CloseTicketHandler;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutoCloseScheduler {

    private static final Long SYSTEM_ACTOR_ID = -1L;

    private final SupportTicketRepository ticketRepository;
    private final CloseTicketHandler closeTicketHandler;

    public AutoCloseScheduler(
            SupportTicketRepository ticketRepository,
            CloseTicketHandler closeTicketHandler
    ) {
        this.ticketRepository = ticketRepository;
        this.closeTicketHandler = closeTicketHandler;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void autoCloseIdleResolvedTickets() {
        ZonedDateTime cutoff = ZonedDateTime.now().minusDays(7);
        List<SupportTicket> idleTickets = ticketRepository.findIdleResolvedTickets(cutoff);

        for (SupportTicket ticket : idleTickets) {
            closeTicketHandler.execute(new CloseTicketCommand(
                    ticket.getId(),
                    SYSTEM_ACTOR_ID,
                    UUID.randomUUID().toString()
            ));
        }
    }
}
