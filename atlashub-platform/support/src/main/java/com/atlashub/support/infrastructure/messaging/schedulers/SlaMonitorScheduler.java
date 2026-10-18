package com.atlashub.support.infrastructure.messaging.schedulers;

import com.atlashub.support.application.command.MarkSlaBreached.MarkSlaBreachedCommand;
import com.atlashub.support.application.command.MarkSlaBreached.MarkSlaBreachedHandler;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SlaMonitorScheduler {

    private final SupportTicketRepository ticketRepository;
    private final MarkSlaBreachedHandler markSlaBreachedHandler;

    public SlaMonitorScheduler(
            SupportTicketRepository ticketRepository,
            MarkSlaBreachedHandler markSlaBreachedHandler
    ) {
        this.ticketRepository = ticketRepository;
        this.markSlaBreachedHandler = markSlaBreachedHandler;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void checkSlaBreaches() {
        List<SupportTicket> overdueTickets = ticketRepository.findSlaOverdueTickets(ZonedDateTime.now());

        for (SupportTicket ticket : overdueTickets) {
            markSlaBreachedHandler.execute(new MarkSlaBreachedCommand(
                    ticket.getId(),
                    "DEADLINE_PASSED",
                    UUID.randomUUID().toString()
            ));
        }
    }
}
