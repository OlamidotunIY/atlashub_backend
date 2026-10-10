package com.atlashub.paystack.infrastructure.external.paystack.dto.common;

public record PaystackMeta(Integer total, Integer skipped, Integer perPage, Integer page, Integer pageCount) {}
