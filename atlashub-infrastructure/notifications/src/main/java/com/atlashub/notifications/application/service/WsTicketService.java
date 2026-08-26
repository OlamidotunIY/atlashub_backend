package com.atlashub.notifications.application.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class WsTicketService {

    private final StringRedisTemplate redisTemplate;
    private static final String TICKET_PREFIX = "ws:ticket:";
    private static final long TICKET_TTL_SECONDS = 30;

    public WsTicketService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateTicket(String userId) {
        String ticket = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(TICKET_PREFIX + ticket, userId, TICKET_TTL_SECONDS, TimeUnit.SECONDS);
        return ticket;
    }

    public String consumeTicket(String ticket) {
        String userId = redisTemplate.opsForValue().get(TICKET_PREFIX + ticket);
        if (userId != null) {
            redisTemplate.delete(TICKET_PREFIX + ticket); // One-time use
        }
        return userId;
    }
}
