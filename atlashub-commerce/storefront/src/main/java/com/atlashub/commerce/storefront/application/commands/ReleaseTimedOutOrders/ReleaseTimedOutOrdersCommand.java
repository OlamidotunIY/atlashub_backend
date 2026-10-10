package com.atlashub.commerce.storefront.application.commands.ReleaseTimedOutOrders;

public record ReleaseTimedOutOrdersCommand(
        int timeoutMinutes
) {
}
