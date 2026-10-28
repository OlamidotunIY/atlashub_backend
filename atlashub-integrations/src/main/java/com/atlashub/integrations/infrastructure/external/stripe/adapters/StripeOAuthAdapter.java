package com.atlashub.integrations.infrastructure.external.stripe.adapters;

import com.atlashub.integrations.application.port.StripeOAuthPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class StripeOAuthAdapter implements StripeOAuthPort {
    @Value("${app.integrations.stripe.test.client-id:}") private String testClientId;
    @Value("${app.integrations.stripe.live.client-id:}") private String liveClientId;
    @Value("${app.integrations.stripe.test.secret-key:}") private String testSecret;
    @Value("${app.integrations.stripe.live.secret-key:}") private String liveSecret;
    @Value("${app.integrations.stripe.redirect-uri:http://localhost:8080/api/v1/integrations/stripe/callback}") private String redirectUri;

    @Override
    public String createAuthorizationUrl(ApiEnvironment environment, String merchantCountry, String state) {
        String clientId = environment == ApiEnvironment.LIVE ? liveClientId : testClientId;
        if (clientId == null || clientId.isBlank()) throw new IllegalStateException("Stripe Connect is not configured for " + environment);
        return UriComponentsBuilder.fromUriString("https://connect.stripe.com/oauth/authorize")
                .queryParam("response_type", "code").queryParam("client_id", clientId)
                .queryParam("scope", "read_write").queryParam("redirect_uri", redirectUri)
                .queryParam("state", state).queryParam("stripe_user[country]", merchantCountry)
                .build().encode().toUriString();
    }

    @Override
    public Token exchange(ApiEnvironment environment, String authorizationCode) {
        String secret = environment == ApiEnvironment.LIVE ? liveSecret : testSecret;
        if (secret == null || secret.isBlank()) throw new IllegalStateException("Stripe Connect secret is not configured");
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", authorizationCode);
        form.add("grant_type", "authorization_code");
        JsonNode token = RestClient.create().post().uri("https://connect.stripe.com/oauth/token")
                .headers(headers -> headers.setBasicAuth(secret, ""))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(JsonNode.class);
        if (token == null) throw new IllegalStateException("Stripe did not return an authorization");
        return new Token(token.path("access_token").asText(), token.path("stripe_user_id").asText(),
                token.path("livemode").asBoolean());
    }
}
