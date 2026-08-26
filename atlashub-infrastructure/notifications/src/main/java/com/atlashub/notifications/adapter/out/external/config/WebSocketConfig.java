package com.atlashub.notifications.adapter.out.external;

import com.atlashub.notifications.application.service.WsTicketService;
import com.atlashub.ratelimiter.core.EvaluateRateLimitUseCase;
import com.atlashub.ratelimiter.core.RateLimitRuleProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;

import java.security.Principal;

@Configuration("notificationsWebSocketConfig")
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WsTicketService ticketService;
    private final EvaluateRateLimitUseCase evaluateRateLimitUseCase;
    private final RateLimitRuleProvider ruleProvider;

    public WebSocketConfig(
            WsTicketService ticketService,
            EvaluateRateLimitUseCase evaluateRateLimitUseCase,
            RateLimitRuleProvider ruleProvider) {
        this.ticketService = ticketService;
        this.evaluateRateLimitUseCase = evaluateRateLimitUseCase;
        this.ruleProvider = ruleProvider;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry the messages back to the client
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
        // Clients subscribe to /user/queue/events
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-events")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new TicketHandshakeInterceptor(ticketService, evaluateRateLimitUseCase, ruleProvider))
                .withSockJS();
                
        // Also support raw websocket without sockjs
        registry.addEndpoint("/ws-events")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new TicketHandshakeInterceptor(ticketService, evaluateRateLimitUseCase, ruleProvider));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract userId put by TicketHandshakeInterceptor
                    String userId = (String) accessor.getSessionAttributes().get("userId");
                    if (userId != null) {
                        Principal user = () -> userId;
                        accessor.setUser(user);
                    }
                }
                return message;
            }
        });
    }
}
