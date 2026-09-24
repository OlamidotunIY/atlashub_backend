package com.atlashub.authentication.infrastructure.security;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OtpTransmissionAdapter implements OtpTransmissionPort {

    private final StringRedisTemplate redisTemplate;

    private static final long TTL_MINUTES = 10;

    public OtpTransmissionAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void storeForTransmission(String correlationId, String rawCode) {
        String key = "otp_transmit:" + correlationId;

        redisTemplate.opsForValue().set(key, rawCode, TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public String retrieveForTransmission(String correlationId) {
        String key = "otp_transmit:" + correlationId;

        // 1. Fetch the raw code
        String rawCode = redisTemplate.opsForValue().get(key);

        if (rawCode != null) {
            redisTemplate.delete(key);
        }

        return rawCode;
    }
}
