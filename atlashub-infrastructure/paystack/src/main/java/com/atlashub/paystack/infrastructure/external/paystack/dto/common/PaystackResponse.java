package com.atlashub.paystack.infrastructure.external.paystack.dto.common;

public record PaystackResponse<T>(boolean status, String message, T data, PaystackMeta meta) {}
