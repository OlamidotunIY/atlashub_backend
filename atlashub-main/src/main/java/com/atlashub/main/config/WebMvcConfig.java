package com.atlashub.main.config;

import com.atlashub.main.interceptor.CorrelationIdInterceptor;
import com.atlashub.ratelimiter.web.GlobalRateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CorrelationIdInterceptor correlationIdInterceptor;
    private final GlobalRateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(CorrelationIdInterceptor correlationIdInterceptor,
                        GlobalRateLimitInterceptor rateLimitInterceptor) {
        this.correlationIdInterceptor = correlationIdInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // CorrelationId runs first on all requests
        registry.addInterceptor(correlationIdInterceptor).addPathPatterns("/**");
        // Global rate limiter runs on all API requests
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
    }
}
