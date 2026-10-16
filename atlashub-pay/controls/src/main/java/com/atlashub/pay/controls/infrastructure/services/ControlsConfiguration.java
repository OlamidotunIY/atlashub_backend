package com.atlashub.pay.controls.infrastructure.services;

import com.atlashub.pay.controls.domain.services.ControlEscalationService;
import com.atlashub.pay.controls.domain.services.FundUsageEvaluationService;
import com.atlashub.pay.controls.domain.services.ProviderCapabilityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControlsConfiguration {

    @Bean
    public FundUsageEvaluationService fundUsageEvaluationService() {
        return new FundUsageEvaluationService();
    }

    @Bean
    public ProviderCapabilityService providerCapabilityService() {
        return new ProviderCapabilityService();
    }

    @Bean
    public ControlEscalationService controlEscalationService() {
        return new ControlEscalationService();
    }
}
