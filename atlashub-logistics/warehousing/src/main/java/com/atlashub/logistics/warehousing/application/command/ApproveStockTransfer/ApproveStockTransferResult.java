package com.atlashub.logistics.warehousing.application.command.ApproveStockTransfer;

public record ApproveStockTransferResult(
        Long transferId,
        String status,
        Long approvedBy
) {}
