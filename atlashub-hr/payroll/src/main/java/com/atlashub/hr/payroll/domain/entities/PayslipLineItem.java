package com.atlashub.hr.payroll.domain.entities;

import com.atlashub.hr.payroll.domain.valueobject.LineItemType;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

@Getter
public class PayslipLineItem {

    private final Long id;
    private final Long payslipId;
    private final String name;
    private final LineItemType type;
    private final Money amount;

    public PayslipLineItem(
            Long id,
            Long payslipId,
            String name,
            LineItemType type,
            Money amount
    ) {
        this.id = id;
        this.payslipId = payslipId;
        this.name = name;
        this.type = type;
        this.amount = amount;
    }

    public static PayslipLineItem create(
            Long id,
            Long payslipId,
            String name,
            LineItemType type,
            Money amount
    ) {
        return new PayslipLineItem(id, payslipId, name, type, amount);
    }
}
