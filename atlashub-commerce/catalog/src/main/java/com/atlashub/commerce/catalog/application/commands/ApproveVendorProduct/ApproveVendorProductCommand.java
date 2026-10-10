package com.atlashub.commerce.catalog.application.commands.ApproveVendorProduct;

public record ApproveVendorProductCommand(Long productId, Long approvedByUserId) {
}
