package com.atlashub.accounting.gl.infrastructure.config;

import com.atlashub.accounting.gl.domain.repositories.LedgerAccountMappingRepository;
import com.atlashub.accounting.gl.domain.services.LedgerMappingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlDomainServiceConfig {

    @Bean
    public LedgerMappingService ledgerMappingService(LedgerAccountMappingRepository mappingRepository) {
        return new LedgerMappingService(mappingRepository);
    }
}
