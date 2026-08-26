package com.atlashub.accounts.application.dto;

public record VirtualAccountDto(Long id, Long integration, String UserCode, String accountName, String nuban, String bankName, String status) {
}
