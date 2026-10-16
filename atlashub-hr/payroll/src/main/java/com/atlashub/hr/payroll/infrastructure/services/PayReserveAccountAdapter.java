package com.atlashub.hr.payroll.infrastructure.services;

import com.atlashub.hr.payroll.application.port.PayrollReserveAccountPort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PayReserveAccountAdapter implements PayrollReserveAccountPort {

    @Override
    public Money getBalance(Long organizationId) {
        return Money.of(new BigDecimal("100000000.00"), CurrencyCode.NGN);
    }
}
