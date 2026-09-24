package com.atlashub.main.config;

import com.atlashub.authentication.domain.ports.OtpGenerator;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class ApplicationConfig {

    // ── JSON ──────────────────────────────────────────────────────────────────

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    // ── Security ──────────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ── HTTP Client ───────────────────────────────────────────────────────────

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ── Firebase Admin SDK ────────────────────────────────────────────────────

    @Bean
    public FirebaseApp firebaseApp(
            @Value("${firebase.credentials.path}") String credentialsPath,
            @Value("${firebase.storage.bucket}") String storageBucket) throws IOException {

        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(storageBucket)
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }

    // ── Domain Services (no Spring annotations — declared here as beans) ──────

    @Bean
    public OtpVerificationIssuer otpVerificationIssuer(OtpGenerator otpGenerator,
                                                        PasswordEncoderPort passwordEncoderPort) {
        return new OtpVerificationIssuer(otpGenerator, passwordEncoderPort);
    }

    // ── Swagger / OpenAPI ─────────────────────────────────────────────────────

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AtlasHub Platform API")
                        .version("2.0")
                        .description("AtlasHub — All-in-one business operating platform for African businesses."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}