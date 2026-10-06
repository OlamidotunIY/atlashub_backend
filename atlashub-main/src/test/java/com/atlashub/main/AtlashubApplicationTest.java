package com.atlashub.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AtlashubApplicationTest {

    @Test
    void disablesSpringBootDefaultUserDetailsService() {
        SpringBootApplication application = AtlashubApplication.class.getAnnotation(SpringBootApplication.class);

        assertThat(application.exclude())
                .contains(UserDetailsServiceAutoConfiguration.class);
    }
}
