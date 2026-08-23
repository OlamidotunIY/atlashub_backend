package com.atlaspay.notifications.infrastructure.config;

import com.atlaspay.notifications.application.service.WsTicketService;
import com.atlaspay.ratelimiter.core.EvaluateRateLimitUseCase;
import com.atlaspay.ratelimiter.core.RateLimitRule;
import com.atlaspay.ratelimiter.core.RateLimitRuleProvider;
import com.atlaspay.shared.exception.RateLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class TicketHandshakeInterceptor implements HandshakeInterceptor {

    private final WsTicketService ticketService;
    private final EvaluateRateLimitUseCase evaluateRateLimitUseCase;
    private final RateLimitRuleProvider ruleProvider;

    public TicketHandshakeInterceptor(
            WsTicketService ticketService,
            EvaluateRateLimitUseCase evaluateRateLimitUseCase,
            RateLimitRuleProvider ruleProvider) {
        this.ticketService = ticketService;
        this.evaluateRateLimitUseCase = evaluateRateLimitUseCase;
        this.ruleProvider = ruleProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                                       
        if (request instanceof ServletServerHttpRequest servletRequest) {
            if (!checkGlobalRateLimit(servletRequest, response)) {
                return false;
            }

            String ticket = servletRequest.getServletRequest().getParameter("ticket");
            if (ticket != null) {
                String userId = ticketService.consumeTicket(ticket);
                if (userId != null) {
                    // Store userId in attributes for the WebSocketSession
                    attributes.put("userId", userId);
                    return true;
                }
            }
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    private boolean checkGlobalRateLimit(ServletServerHttpRequest request, ServerHttpResponse response) {
        try {
            RateLimitRule rule = ruleProvider.getRule("global_ip_tb");
            String key = getClientIp(request) + ":global_ip_tb";
            var result = evaluateRateLimitUseCase.execute(key, rule);

            response.getHeaders().set("X-RateLimit-Limit", String.valueOf(result.limit()));
            response.getHeaders().set("X-RateLimit-Remaining", String.valueOf(result.remainingRequests()));
            return true;
        } catch (RateLimitExceededException ex) {
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().set("X-RateLimit-Limit", String.valueOf(ex.getLimit()));
            response.getHeaders().set("X-RateLimit-Remaining", "0");
            response.getHeaders().set("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
            return false;
        }
    }

    private String getClientIp(ServletServerHttpRequest request) {
        String forwardedFor = request.getServletRequest().getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getServletRequest().getRemoteAddr();
        }
        return forwardedFor.split(",")[0].trim();
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
