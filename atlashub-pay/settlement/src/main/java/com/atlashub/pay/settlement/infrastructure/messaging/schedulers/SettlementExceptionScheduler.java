package com.atlashub.pay.settlement.infrastructure.messaging.schedulers;
import com.atlashub.pay.settlement.application.commands.EscalateUnmatchedSettlements.EscalateUnmatchedSettlementsCommand;
import com.atlashub.pay.settlement.application.commands.EscalateUnmatchedSettlements.EscalateUnmatchedSettlementsHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component public class SettlementExceptionScheduler{
    private final EscalateUnmatchedSettlementsHandler handler;
    public SettlementExceptionScheduler(EscalateUnmatchedSettlementsHandler handler){this.handler=handler;}
    @Scheduled(cron="${atlashub.pay.settlement.escalation-cron:0 0 6 * * *}")
    public void escalate(){handler.execute(new EscalateUnmatchedSettlementsCommand());}
}
