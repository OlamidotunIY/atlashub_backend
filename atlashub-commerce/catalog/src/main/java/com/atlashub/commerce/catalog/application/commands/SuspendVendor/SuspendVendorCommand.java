package com.atlashub.commerce.catalog.application.commands.SuspendVendor;

public record SuspendVendorCommand(Long vendorId, String reason) {
}
