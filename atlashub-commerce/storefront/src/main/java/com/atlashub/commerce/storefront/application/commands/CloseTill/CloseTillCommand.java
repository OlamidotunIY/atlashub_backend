package com.atlashub.commerce.storefront.application.commands.CloseTill;

import com.atlashub.shared.domain.valueobject.Money;

public record CloseTillCommand(
        Long tillId,
        Long closedBy,
        Money actualClosingBalance
) {
}
