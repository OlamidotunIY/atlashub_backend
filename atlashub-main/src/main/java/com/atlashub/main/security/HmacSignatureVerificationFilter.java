package com.atlashub.main.security;

import com.atlashub.iam.application.security.ApiKeyAuthenticationService;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.application.service.HashingUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class HmacSignatureVerificationFilter extends OncePerRequestFilter {
    private static final long TOLERANCE_SECONDS = 300;
    private final ApiKeyAuthenticationService authenticationService;
    private final StringRedisTemplate redisTemplate;

    public HmacSignatureVerificationFilter(ApiKeyAuthenticationService authenticationService,
                                           StringRedisTemplate redisTemplate) {
        this.authenticationService = authenticationService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("AtlasHmac ")) {
            chain.doFilter(request, response);
            return;
        }
        try {
            Map<String, String> values = parse(authorization.substring("AtlasHmac ".length()));
            String publicKey = required(values, "publicKey");
            String nonce = required(values, "nonce");
            String signature = required(values, "signature");
            long timestamp = Long.parseLong(required(values, "timestamp"));
            if (Math.abs(Instant.now().getEpochSecond() - timestamp) > TOLERANCE_SECONDS) {
                unauthorized(response, "HMAC_TIMESTAMP_EXPIRED"); return;
            }
            Boolean fresh = redisTemplate.opsForValue().setIfAbsent(
                    "hmac:nonce:" + publicKey + ":" + nonce, "1", Duration.ofSeconds(360));
            if (!Boolean.TRUE.equals(fresh)) {
                unauthorized(response, "HMAC_NONCE_REPLAYED"); return;
            }

            CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);
            String body = new String(cached.body(), StandardCharsets.UTF_8);
            String canonical = request.getMethod().toUpperCase() + "\n" + request.getRequestURI()
                    + "\n" + timestamp + "\n" + nonce + "\n" + HashingUtils.sha256Hex(body);
            var authenticated = authenticationService.authenticate(publicKey, canonical, signature);
            if (authenticated.isEmpty()) {
                unauthorized(response, "HMAC_SIGNATURE_INVALID"); return;
            }
            var key = authenticated.get();
            var authorities = key.permissions().stream().map(SimpleGrantedAuthority::new).toList();
            AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                    null, key.organizationId(), key.environment(), null, null, null);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, authorities));
            chain.doFilter(cached, response);
        } catch (IllegalArgumentException error) {
            unauthorized(response, "HMAC_HEADER_INVALID");
        }
    }

    private Map<String, String> parse(String value) {
        Map<String, String> result = new HashMap<>();
        for (String part : value.split(",")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length == 2) result.put(pair[0], pair[1]);
        }
        return result;
    }

    private String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing " + key);
        return value;
    }

    private void unauthorized(HttpServletResponse response, String code) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"code\":\"" + code + "\"}");
    }
}
