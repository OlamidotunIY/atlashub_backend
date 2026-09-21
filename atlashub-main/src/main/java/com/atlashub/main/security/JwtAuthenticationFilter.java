package com.atlashub.main.security;

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


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Inject whatever service you use to decode JWTs here
    // private final JwtDecoder jwtDecoder;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        try {
            // 1. Decode the token (Using your JWT library)
            // Claims claims = jwtDecoder.decode(token);

            // 2. Extract Data (Assuming your payload has permissions)
            // String userId = claims.getSubject();
            // List<String> permissions = claims.get("permissions", List.class);

            // 3. Convert permissions to Spring Security Authorities
            // List<SimpleGrantedAuthority> authorities = permissions.stream()
            //      .map(SimpleGrantedAuthority::new)
            //      .collect(Collectors.toList());
            // 4. Set the security context
            // UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            // SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            // If token is expired or tampered with, clear context. EntryPoint will return 401.
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
