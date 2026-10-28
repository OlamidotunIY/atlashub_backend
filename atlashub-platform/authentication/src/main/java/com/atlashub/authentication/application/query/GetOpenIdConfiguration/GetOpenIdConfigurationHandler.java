package com.atlashub.authentication.application.query.GetOpenIdConfiguration;

import com.atlashub.shared.application.usecase.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetOpenIdConfigurationHandler extends Query<GetOpenIdConfigurationQuery, GetOpenIdConfigurationResult> {

    private final String issuer;

    public GetOpenIdConfigurationHandler(
            @Value("${atlashub.jwt.issuer:atlashub-authentication}") String issuer) {
        this.issuer = issuer;
    }

    @Override
    public GetOpenIdConfigurationResult execute(GetOpenIdConfigurationQuery query) {
        String base = (query.baseUrl() != null && !query.baseUrl().isBlank())
                ? query.baseUrl().replaceAll("/+$", "")
                : issuer;

        return new GetOpenIdConfigurationResult(
                issuer,
                base + "/api/v1/oauth2/authorize",
                base + "/api/v1/oauth2/token",
                base + "/api/v1/oauth2/userinfo",
                base + "/.well-known/jwks.json",
                List.of("code"),
                List.of("public"),
                List.of("RS256"),
                List.of("S256"),
                List.of("openid", "profile", "email", "offline_access"),
                List.of("none", "client_secret_post", "client_secret_basic"),
                List.of("authorization_code", "refresh_token")
        );
    }
}
