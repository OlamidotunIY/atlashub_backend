package com.atlashub.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Tell Spring to scan ALL modules starting with "com.atlashub"
@SpringBootApplication(scanBasePackages = "com.atlashub")
@EntityScan(basePackages = "com.atlashub")
@EnableJpaRepositories(basePackages = "com.atlashub")
public class AtlashubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlashubApplication.class, args);
    }
}