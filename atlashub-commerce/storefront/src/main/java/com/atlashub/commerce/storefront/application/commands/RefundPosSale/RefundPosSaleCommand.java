package com.atlashub.commerce.storefront.application.commands.RefundPosSale;

public record RefundPosSaleCommand(
        Long salesOrderId,
        String reason,
        String customerNuban,
        String customerBankCode
) {
}
