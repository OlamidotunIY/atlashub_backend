package com.atlashub.notifications.infrastructure.messaging.schedulers;

import com.atlashub.notifications.application.command.RetryRegistrationOtpDeliveries.RetryRegistrationOtpDeliveriesCommand;
import com.atlashub.notifications.application.command.RetryRegistrationOtpDeliveries.RetryRegistrationOtpDeliveriesHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RegistrationOtpRetryScheduler {
    private final RetryRegistrationOtpDeliveriesHandler handler;

    public RegistrationOtpRetryScheduler(RetryRegistrationOtpDeliveriesHandler handler) {
        this.handler = handler;
    }

    @Scheduled(cron = "${atlashub.notifications.otp-retry-cron:0 */5 * * * *}")
    public void retryFailedOtpDeliveries() {
        handler.execute(new RetryRegistrationOtpDeliveriesCommand());
    }
}
