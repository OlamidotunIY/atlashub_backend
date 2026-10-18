package com.atlashub.support.application.query.GetSlaMetrics;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.domain.repositories.SupportTicketRepository;
import com.atlashub.support.domain.valueobject.TicketCategory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GetSlaMetricsHandler extends Query<GetSlaMetricsQuery, SlaMetricsResult> {

    private final SupportTicketRepository ticketRepository;

    public GetSlaMetricsHandler(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public SlaMetricsResult execute(GetSlaMetricsQuery query) {
        List<SupportTicket> tickets = ticketRepository.findCreatedBetween(query.dateFrom(), query.dateTo());

        int total = tickets.size();
        if (total == 0) {
            return new SlaMetricsResult(0, 100.0, 100.0, 0.0, 0.0, 0, List.of());
        }

        int breachedCount = 0;
        int firstResponseWithinSla = 0;
        int resolutionWithinSla = 0;
        long totalFirstResponseMinutes = 0;
        int firstResponseCount = 0;
        long totalResolutionMinutes = 0;
        int resolvedCount = 0;

        Map<TicketCategory, Integer> categoryTotal = new HashMap<>();
        Map<TicketCategory, Integer> categoryBreached = new HashMap<>();

        for (SupportTicket t : tickets) {
            categoryTotal.merge(t.getCategory(), 1, Integer::sum);

            if (t.isSlaBreached()) {
                breachedCount++;
                categoryBreached.merge(t.getCategory(), 1, Integer::sum);
            }

            if (t.getFirstRespondedAt() != null) {
                firstResponseCount++;
                long mins = Math.max(0, Duration.between(t.getCreatedAt(), t.getFirstRespondedAt()).toMinutes());
                totalFirstResponseMinutes += mins;
                if (!t.getFirstRespondedAt().isAfter(t.getFirstResponseDeadline())) {
                    firstResponseWithinSla++;
                }
            } else if (t.getFirstResponseDeadline().isBefore(java.time.ZonedDateTime.now())) {
                // Response deadline passed without response
            } else {
                firstResponseWithinSla++;
            }

            if (t.getResolvedAt() != null) {
                resolvedCount++;
                long mins = Math.max(0, Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes());
                totalResolutionMinutes += mins;
                if (!t.getResolvedAt().isAfter(t.getResolutionDeadline())) {
                    resolutionWithinSla++;
                }
            }
        }

        double firstResponseRate = (double) firstResponseWithinSla / total * 100.0;
        double resolutionRate = resolvedCount > 0 ? (double) resolutionWithinSla / resolvedCount * 100.0 : 100.0;
        double avgFirstResponse = firstResponseCount > 0 ? (double) totalFirstResponseMinutes / firstResponseCount : 0.0;
        double avgResolution = resolvedCount > 0 ? (double) totalResolutionMinutes / resolvedCount : 0.0;

        List<SlaCategoryBreakdownResult> byCategory = new ArrayList<>();
        for (TicketCategory cat : TicketCategory.values()) {
            int catTot = categoryTotal.getOrDefault(cat, 0);
            if (catTot > 0) {
                int catBrk = categoryBreached.getOrDefault(cat, 0);
                double compRate = ((double) (catTot - catBrk) / catTot) * 100.0;
                byCategory.add(new SlaCategoryBreakdownResult(cat, catTot, catBrk, compRate));
            }
        }

        return new SlaMetricsResult(
                total,
                firstResponseRate,
                resolutionRate,
                avgFirstResponse,
                avgResolution,
                breachedCount,
                byCategory
        );
    }
}
