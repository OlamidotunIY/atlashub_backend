package com.atlashub.hr.staff.infrastructure.services;

import com.atlashub.hr.staff.application.port.BankVerificationPort;
import org.springframework.stereotype.Component;

@Component
public class DefaultBankVerificationAdapter implements BankVerificationPort {

    @Override
    public String verifyAccount(String bankCode, String accountNumber) {
        if (bankCode == null || bankCode.isBlank() || accountNumber == null || accountNumber.isBlank()) {
            return null;
        }
        return "VERIFIED ACCOUNT " + accountNumber;
    }
}
