package com.atlashub.commerce.catalog.application.commands.CreateSupplier;

public record CreateSupplierCommand(
        Long organizationId,
        String name,
        String email,
        String phone,
        String address
) {
}
