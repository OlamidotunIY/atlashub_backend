package com.atlashub.shared.domain.money;

import com.atlashub.shared.domain.exception.SharedErrorCode;
import com.atlashub.shared.domain.exception.ValidationException;
import java.math.BigDecimal;

public class NegativeMoneyException extends ValidationException {

    public NegativeMoneyException(BigDecimal amount) {
        super(SharedErrorCode.MONEY_NEGATIVE_AMOUNT, "Money amount cannot be negative: " + amount);
    }
}
