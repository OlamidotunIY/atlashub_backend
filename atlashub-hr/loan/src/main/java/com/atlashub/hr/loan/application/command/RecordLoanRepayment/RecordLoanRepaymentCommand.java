package com.atlashub.hr.loan.application.command.RecordLoanRepayment;

import com.atlashub.shared.domain.valueobject.Money;

public record RecordLoanRepaymentCommand(Long loanId, Money amount) {}
