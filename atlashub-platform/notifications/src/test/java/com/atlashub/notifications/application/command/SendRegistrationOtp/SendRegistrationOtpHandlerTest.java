package com.atlashub.notifications.application.command.SendRegistrationOtp;

import com.atlashub.notifications.domain.entities.NotificationDelivery;
import com.atlashub.notifications.domain.ports.EmailPort;
import com.atlashub.notifications.domain.repositories.NotificationDeliveryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendRegistrationOtpHandlerTest {
    @Mock private NotificationDeliveryRepository deliveryRepository;
    @Mock private EmailPort emailPort;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private StringRedisTemplate redisTemplate;

    @Test
    void sendsRenderedOtpAndRecordsDelivery() {
        when(deliveryRepository.findByCorrelationId("correlation-1")).thenReturn(Optional.empty());
        when(deliveryRepository.nextIdentity()).thenReturn(1L);
        when(redisTemplate.opsForValue().get("otp_transmit:correlation-1")).thenReturn("123456");
        SendRegistrationOtpHandler handler = new SendRegistrationOtpHandler(deliveryRepository, emailPort, redisTemplate);

        handler.execute(new SendRegistrationOtpCommand("person@example.com", "correlation-1", Instant.now().plusSeconds(600).getEpochSecond()));

        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        verify(emailPort).sendHtml(eq("person@example.com"), eq("Verify your AtlasHub email"), html.capture());
        assertThat(html.getValue()).contains("123456").doesNotContain("{{otp}}");
        ArgumentCaptor<NotificationDelivery> delivery = ArgumentCaptor.forClass(NotificationDelivery.class);
        verify(deliveryRepository).save(delivery.capture());
        assertThat(delivery.getValue().getStatus().name()).isEqualTo("DELIVERED");
    }
}
