package com.atlashub.notifications.application.command.SendRegistrationOtp;

import com.atlashub.notifications.domain.entities.NotificationDelivery;
import com.atlashub.notifications.domain.exceptions.OtpDeliveryException;
import com.atlashub.notifications.domain.ports.EmailPort;
import com.atlashub.notifications.domain.repositories.NotificationDeliveryRepository;
import com.atlashub.notifications.domain.valueobject.DeliveryStatus;
import com.atlashub.notifications.domain.valueobject.NotificationChannel;
import com.atlashub.notifications.domain.valueobject.RecipientType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

@Component
public class SendRegistrationOtpHandler extends Command<SendRegistrationOtpCommand, Void> {
    private static final int MAX_ATTEMPTS = 5;
    private static final String TEMPLATE_CODE = "REGISTRATION_OTP";
    private static final String SUBJECT = "Verify your AtlasHub email";
    private final NotificationDeliveryRepository deliveryRepository;
    private final EmailPort emailPort;
    private final StringRedisTemplate redisTemplate;

    public SendRegistrationOtpHandler(NotificationDeliveryRepository deliveryRepository, EmailPort emailPort, StringRedisTemplate redisTemplate) {
        this.deliveryRepository = deliveryRepository; this.emailPort = emailPort; this.redisTemplate = redisTemplate;
    }

    @Override @Transactional
    public Void execute(SendRegistrationOtpCommand command) {
        NotificationDelivery delivery = deliveryRepository.findByCorrelationId(command.correlationId()).orElseGet(() -> createDelivery(command));
        if (delivery.getStatus() == DeliveryStatus.DELIVERED || delivery.getStatus() == DeliveryStatus.PERMANENTLY_FAILED) return null;
        String otp = redisTemplate.opsForValue().get("otp_transmit:" + command.correlationId());
        if (otp == null || otp.isBlank()) throw new OtpDeliveryException("Registration OTP is no longer available");
        try {
            emailPort.sendHtml(command.recipientEmail(), SUBJECT, delivery.getRenderedBody().replace("{{otp}}", otp));
            delivery.markDelivered(null); deliveryRepository.save(delivery); return null;
        } catch (RuntimeException ex) {
            if (delivery.getAttemptCount() >= MAX_ATTEMPTS - 1) delivery.markPermanentlyFailed(safeMessage(ex));
            else delivery.markFailedAndScheduleRetry(safeMessage(ex), ZonedDateTime.now().plus(backoff(delivery.getAttemptCount())));
            deliveryRepository.save(delivery);
            throw new OtpDeliveryException("Unable to deliver registration OTP", ex);
        }
    }

    private NotificationDelivery createDelivery(SendRegistrationOtpCommand command) {
        return NotificationDelivery.create(deliveryRepository.nextIdentity(), TEMPLATE_CODE, command.correlationId(), 1,
                RecipientType.EXTERNAL_EMAIL, command.recipientEmail(), NotificationChannel.EMAIL, "SMTP", Map.of(), SUBJECT,
                render("{{otp}}", command.expiresAtEpochSeconds()));
    }
    private String render(String otp, long expiry) {
        try {
            String template = new String(new ClassPathResource("templates/email/registration-otp.html").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            long minutes = Math.max(1, Duration.between(java.time.Instant.now(), java.time.Instant.ofEpochSecond(expiry)).toMinutes());
            return template.replace("{{otp}}", otp).replace("{{expiryMinutes}}", Long.toString(minutes));
        } catch (IOException ex) { throw new OtpDeliveryException("Registration OTP template is unavailable", ex); }
    }
    private Duration backoff(int attempts) { return switch (attempts) { case 0 -> Duration.ofSeconds(5); case 1 -> Duration.ofSeconds(30); case 2 -> Duration.ofMinutes(5); case 3 -> Duration.ofMinutes(30); default -> Duration.ofHours(2); }; }
    private String safeMessage(Exception ex) { return ex.getClass().getSimpleName(); }
}
