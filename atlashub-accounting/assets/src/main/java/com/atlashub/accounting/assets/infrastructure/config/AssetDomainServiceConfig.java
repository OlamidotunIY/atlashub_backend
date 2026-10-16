package com.atlashub.accounting.assets.infrastructure.config;

import com.atlashub.accounting.assets.domain.services.DepreciationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssetDomainServiceConfig {

    @Bean
    public DepreciationService depreciationService() {
        return new DepreciationService();
    }
}
