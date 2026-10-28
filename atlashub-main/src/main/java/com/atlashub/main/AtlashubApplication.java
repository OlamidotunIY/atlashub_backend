package com.atlashub.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

// Tell Spring to scan ALL modules starting with "com.atlashub"
@SpringBootApplication(
        scanBasePackages = "com.atlashub",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
        exclude = UserDetailsServiceAutoConfiguration.class
)
@EntityScan(basePackages = "com.atlashub")
@EnableJpaRepositories(
        basePackages = "com.atlashub",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
@EnableScheduling
public class AtlashubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlashubApplication.class, args);
    }
}
