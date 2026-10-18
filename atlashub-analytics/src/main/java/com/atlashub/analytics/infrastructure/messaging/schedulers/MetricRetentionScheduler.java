package com.atlashub.analytics.infrastructure.messaging.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MetricRetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(MetricRetentionScheduler.class);

    @Scheduled(cron = "0 0 4 * * SUN")
    public void cleanupRetainedMetrics() {
        log.info("Executing scheduled metric retention verification");
    }
}
