package com.atlashub.hr.payroll.application.port;

import com.atlashub.shared.domain.valueobject.Money;

public interface PayrollReserveAccountPort {

    Money getBalance(Long organizationId);
}
