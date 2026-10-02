package com.atlashub.main.security;

import com.atlashub.authentication.infrastructure.security.JwtTokenAdapter;
import com.atlashub.authentication.application.port.TokenRevocationPort;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenAdapter jwtTokenAdapter;
    private final TokenRevocationPort revocationPort;
    private final SessionRepository sessionRepository;

    public JwtAuthenticationFilter(JwtTokenAdapter jwtTokenAdapter,
                                   TokenRevocationPort revocationPort,
                                   SessionRepository sessionRepository) {
        this.jwtTokenAdapter = jwtTokenAdapter;
        this.revocationPort = revocationPort;
        this.sessionRepository = sessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            var parsed = Jwts.parser()
                    .verifyWith(jwtTokenAdapter.getPublicKey())
                    .build()
                    .parseSignedClaims(token);
            Claims claims = parsed.getPayload();
            if (!jwtTokenAdapter.issuer().equals(claims.getIssuer())
                    || claims.getAudience() == null
                    || !claims.getAudience().contains(jwtTokenAdapter.audience())
                    || !"at+jwt".equals(parsed.getHeader().getType())) {
                throw new JwtException("Invalid token issuer, audience, or type");
            }

            String jti = claims.getId();
            if (jti != null && revocationPort.isRevoked(jti)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            String userId = claims.getSubject();
            String sessionId = claims.get("sid", String.class);
            String organizationId = claims.get("org", String.class);
            String environment = claims.get("env", String.class);

            if (sessionId == null || sessionId.isBlank()
                    || organizationId == null || organizationId.isBlank()
                    || environment == null || environment.isBlank()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Session session = sessionRepository.findById(Long.valueOf(sessionId))
                    .filter(activeSession -> !activeSession.isExpired())
                    .filter(activeSession -> activeSession.getUserId().equals(userId))
                    .filter(activeSession -> activeSession.getOrganizationId().equals(Long.valueOf(organizationId)))
                    .filter(activeSession -> activeSession.getEnvironment().equals(environment))
                    .orElse(null);

            if (session == null) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            @SuppressWarnings("unchecked")
            List<String> permissions = claims.get("permissions", List.class);
            if (permissions == null) permissions = Collections.emptyList();

            List<SimpleGrantedAuthority> authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                    Long.valueOf(userId), Long.valueOf(organizationId), environment, sessionId);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
