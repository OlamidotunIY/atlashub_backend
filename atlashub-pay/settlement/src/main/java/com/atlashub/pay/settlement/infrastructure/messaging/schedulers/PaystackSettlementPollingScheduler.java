package com.atlashub.pay.settlement.infrastructure.messaging.schedulers;

import com.atlashub.pay.settlement.application.commands.PollPaystackSettlements.PollPaystackSettlementsCommand;
import com.atlashub.pay.settlement.application.commands.PollPaystackSettlements.PollPaystackSettlementsHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaystackSettlementPollingScheduler {
    private final PollPaystackSettlementsHandler handler;
    public PaystackSettlementPollingScheduler(PollPaystackSettlementsHandler handler){this.handler=handler;}
    @Scheduled(cron="${atlashub.pay.settlement.polling-cron:0 */15 * * * *}")
    public void poll(){handler.execute(new PollPaystackSettlementsCommand());}
}
