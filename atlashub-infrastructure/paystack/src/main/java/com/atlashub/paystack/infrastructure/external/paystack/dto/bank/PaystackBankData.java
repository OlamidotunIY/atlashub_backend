package com.atlashub.paystack.infrastructure.external.paystack.dto.bank;

public record PaystackBankData(Long id, String name, String slug, String code, String country, String currency,
                               boolean active) {}
