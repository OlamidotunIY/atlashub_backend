package com.atlashub.commerce.storefront.infrastructure.messaging.schedulers;

import com.atlashub.commerce.storefront.application.commands.ReleaseTimedOutOrders.ReleaseTimedOutOrdersCommand;
import com.atlashub.commerce.storefront.application.commands.ReleaseTimedOutOrders.ReleaseTimedOutOrdersHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StockReleaseScheduler {

    private static final Logger log = LoggerFactory.getLogger(StockReleaseScheduler.class);
    private static final int DEFAULT_TIMEOUT_MINUTES = 15;

    private final ReleaseTimedOutOrdersHandler handler;

    public StockReleaseScheduler(ReleaseTimedOutOrdersHandler handler) {
        this.handler = Objects.requireNonNull(handler, "ReleaseTimedOutOrdersHandler must not be null");
    }

    @Scheduled(cron = "${commerce.storefront.stock-release-cron:0 * * * * *}")
    public void releaseTimedOutOrders() {
        log.debug("Executing scheduled release of timed-out storefront orders older than {} minutes", DEFAULT_TIMEOUT_MINUTES);
        handler.execute(new ReleaseTimedOutOrdersCommand(DEFAULT_TIMEOUT_MINUTES));
    }
}
