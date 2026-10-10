package com.atlashub.pay.settlement.application.commands.EscalateUnmatchedSettlements;

import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

@Component
public class EscalateUnmatchedSettlementsHandler extends Command<EscalateUnmatchedSettlementsCommand, Integer> {
    private final SettlementRepository repository;

    public EscalateUnmatchedSettlementsHandler(SettlementRepository repository) {
        this.repository = repository;
    }

    @Override
    public Integer execute(EscalateUnmatchedSettlementsCommand ignored) {
        ZonedDateTime cutoff = threeBusinessDaysBefore(ZonedDateTime.now());
        int count = 0;
        for (var settlement : repository.findAwaitingAnchorCreditCreatedBefore(cutoff)) {
            settlement.requireReconciliation("Anchor credit not matched within three business days");
            repository.save(settlement);
            count++;
        }
        return count;
    }

    private ZonedDateTime threeBusinessDaysBefore(ZonedDateTime from) {
        int days = 0;
        ZonedDateTime cursor = from;
        while (days < 3) {
            cursor = cursor.minusDays(1);
            DayOfWeek day = cursor.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) days++;
        }
        return cursor;
    }
}
