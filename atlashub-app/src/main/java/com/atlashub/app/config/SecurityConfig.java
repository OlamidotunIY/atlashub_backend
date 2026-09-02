package com.atlashub.app.config;

import com.atlashub.app.security.filter.ApiKeyAuthenticationFilter;
import com.atlashub.app.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, ApiKeyAuthenticationFilter apiKeyAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.apiKeyAuthFilter = apiKeyAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PublicEndpointScanner scanner) throws Exception {
        
        List<PublicEndpointScanner.EndpointConfig> publicEndpoints = scanner.getPublicEndpoints();
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                
                // 1. Register dynamically scanned public endpoints
                for (PublicEndpointScanner.EndpointConfig config : publicEndpoints) {
                    if (config.method() != null) {
                        auth.requestMatchers(config.method(), config.path()).permitAll();
                    } else {
                        auth.requestMatchers(config.path()).permitAll();
                    }
                }
                
                // 1.5. Permit Error dispatchers so 404s don't become 403s
                auth.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll();

                // 2. Register hardcoded default public endpoints
                auth.requestMatchers(
                    org.springframework.http.HttpMethod.POST,
                    "/api/v1/users"
                ).permitAll();
                
                auth.requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/admins/auth/bootstrap",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/**"
                ).permitAll();
                
                // 3. Secure everything else
                auth.anyRequest().authenticated();
            })
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4000", "http://localhost:*", "https://*.atlashub.name.ng")); // Allow frontend origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "x-api-key", "Accept", "Origin", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
