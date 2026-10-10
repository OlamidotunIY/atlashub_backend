package com.atlashub.commerce.storefront.application.commands.OpenTill;

import com.atlashub.shared.domain.valueobject.Money;

public record OpenTillCommand(
        Long organizationId,
        Long outletId,
        String name,
        Long openedBy,
        Money openingFloat
) {
}
