package com.atlashub.analytics.infrastructure.messaging.schedulers;

import com.atlashub.analytics.application.command.CreateBusinessFeatureSnapshot.CreateBusinessFeatureSnapshotCommand;
import com.atlashub.analytics.application.command.CreateBusinessFeatureSnapshot.CreateBusinessFeatureSnapshotHandler;
import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class FeatureSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(FeatureSnapshotScheduler.class);

    private final CreateBusinessFeatureSnapshotHandler snapshotHandler;

    public FeatureSnapshotScheduler(CreateBusinessFeatureSnapshotHandler snapshotHandler) {
        this.snapshotHandler = snapshotHandler;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void generateDailyFeatureSnapshots() {
        log.info("Executing scheduled feature snapshot generation");
        ZonedDateTime now = ZonedDateTime.now();
        snapshotHandler.execute(new CreateBusinessFeatureSnapshotCommand(
                0L,
                "live",
                "v1.0",
                now.minusDays(30),
                now,
                now,
                DataQualityStatus.COMPLETE,
                "{\"status\":\"scheduled_snapshot\"}",
                "{}",
                now.plusDays(90),
                UUID.randomUUID().toString()
        ));
    }
}
