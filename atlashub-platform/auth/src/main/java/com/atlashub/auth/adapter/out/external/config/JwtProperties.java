package com.atlashub.auth.adapter.out.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "atlashub.jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpirationMs = 1800000; // 30 minutes
    private long refreshTokenExpirationMs = 604800000; // 7 days
    private long preAuthTokenExpirationMs = 300000; // 5 minutes
}
