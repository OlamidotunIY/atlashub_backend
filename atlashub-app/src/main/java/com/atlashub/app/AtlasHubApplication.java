package com.atlashub.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * AtlasHub composition root.
 *
 * <p>The {@code @SpringBootApplication} scan is intentionally scoped to {@code com.atlashub}
 * to pick up all module components (use cases, adapters, controllers) registered as Spring beans.
 */
@SpringBootApplication(scanBasePackages = "com.atlashub")
@EntityScan(basePackages = "com.atlashub")
@EnableJpaRepositories(basePackages = "com.atlashub")
public class AtlasHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasHubApplication.class, args);
    }
}
