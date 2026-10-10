package com.atlashub.paystack.infrastructure.external.paystack.client;

public record PaystackClients(PaystackBankClient banks, PaystackSubaccountClient subaccounts,
                              PaystackTransactionClient transactions, PaystackSettlementClient settlements,
                              PaystackRefundClient refunds) {
}
