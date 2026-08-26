package com.atlashub.shared.money;

import com.atlashub.shared.exception.SharedErrorCode;
import com.atlashub.shared.exception.ValidationException;
import java.math.BigDecimal;

public class NegativeMoneyException extends ValidationException {

    public NegativeMoneyException(BigDecimal amount) {
        super(SharedErrorCode.MONEY_NEGATIVE_AMOUNT, "Money amount cannot be negative: " + amount);
    }
}
