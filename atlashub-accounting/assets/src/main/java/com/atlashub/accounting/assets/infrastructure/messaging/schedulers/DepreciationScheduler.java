package com.atlashub.accounting.assets.infrastructure.messaging.schedulers;

import com.atlashub.accounting.assets.application.command.RunDepreciation.RunDepreciationCommand;
import com.atlashub.accounting.assets.application.command.RunDepreciation.RunDepreciationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
public class DepreciationScheduler {

    private static final Logger log = LoggerFactory.getLogger(DepreciationScheduler.class);

    private final RunDepreciationHandler handler;

    public DepreciationScheduler(RunDepreciationHandler handler) {
        this.handler = Objects.requireNonNull(handler, "RunDepreciationHandler must not be null");
    }

    @Scheduled(cron = "${accounting.assets.depreciation-cron:0 0 1 * * *}")
    public void runMonthlyDepreciation() {
        log.info("Triggering monthly depreciation run");
        handler.execute(new RunDepreciationCommand(null, LocalDate.now()));
    }
}
