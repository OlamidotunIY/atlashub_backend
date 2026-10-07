package com.atlashub.notifications.application.command.RetryRegistrationOtpDeliveries;

import com.atlashub.notifications.application.command.SendRegistrationOtp.SendRegistrationOtpCommand;
import com.atlashub.notifications.application.command.SendRegistrationOtp.SendRegistrationOtpHandler;
import com.atlashub.notifications.domain.repositories.NotificationDeliveryRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class RetryRegistrationOtpDeliveriesHandler extends Command<RetryRegistrationOtpDeliveriesCommand, Void> {
    private final NotificationDeliveryRepository deliveryRepository;
    private final SendRegistrationOtpHandler sendHandler;

    public RetryRegistrationOtpDeliveriesHandler(NotificationDeliveryRepository deliveryRepository, SendRegistrationOtpHandler sendHandler) {
        this.deliveryRepository = deliveryRepository;
        this.sendHandler = sendHandler;
    }

    @Override
    public Void execute(RetryRegistrationOtpDeliveriesCommand command) {
        deliveryRepository.findRetryDueBefore(ZonedDateTime.now()).forEach(delivery -> sendHandler.execute(new SendRegistrationOtpCommand(delivery.getRecipientId(), delivery.getCorrelationId(), delivery.getNextRetryAt().toEpochSecond())));
        return null;
    }
}
