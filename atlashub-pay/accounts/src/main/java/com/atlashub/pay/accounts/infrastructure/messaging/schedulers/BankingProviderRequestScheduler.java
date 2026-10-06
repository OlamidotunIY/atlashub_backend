package com.atlashub.pay.accounts.infrastructure.messaging.schedulers;

import com.atlashub.pay.accounts.application.commands.DispatchBankingProviderRequests.DispatchBankingProviderRequestsCommand;
import com.atlashub.pay.accounts.application.commands.DispatchBankingProviderRequests.DispatchBankingProviderRequestsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class BankingProviderRequestScheduler {
    private static final Logger log = LoggerFactory.getLogger(BankingProviderRequestScheduler.class);
    private final DispatchBankingProviderRequestsHandler handler;

    public BankingProviderRequestScheduler(DispatchBankingProviderRequestsHandler handler) {
        this.handler = handler;
    }

    @Scheduled(cron = "${pay.accounts.provider-dispatch-cron:*/5 * * * * *}")
    public void dispatch() {
        log.info("Dispatching pending banking provider requests");
        handler.execute(new DispatchBankingProviderRequestsCommand(25));
    }
}
