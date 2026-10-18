package com.atlashub.hr.payroll.application.query.GetPayslip;

import com.atlashub.hr.payroll.domain.valueobject.LineItemType;
import com.atlashub.shared.domain.valueobject.Money;

public record PayslipLineItemResult(
        String name,
        LineItemType type,
        Money amount
) {
}
