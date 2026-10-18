package com.atlashub.hr.payroll.infrastructure.config;

import com.atlashub.hr.payroll.domain.services.PayrollCalculationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayrollConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PayrollCalculationService payrollCalculationService() {
        return new PayrollCalculationService();
    }
}
