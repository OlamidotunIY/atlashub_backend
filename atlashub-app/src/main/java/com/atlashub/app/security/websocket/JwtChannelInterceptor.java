package com.atlashub.app.security.websocket;

import com.atlashub.app.security.authentication.AtlasHubAuthenticationToken;
import com.atlashub.app.security.AuthTokenParser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final AuthTokenParser authTokenParser;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                String OrganizationId = authTokenParser.extractPrincipalId(jwt);
                
                if (OrganizationId != null && authTokenParser.isTokenValid(jwt)) {
                    AtlasHubAuthenticationToken authToken = new AtlasHubAuthenticationToken(
                            OrganizationId,
                            jwt,
                            AtlasHubAuthenticationToken.AuthType.JWT
                    );
                    accessor.setUser(authToken);
                }
            }
        }
        return message;
    }
}

