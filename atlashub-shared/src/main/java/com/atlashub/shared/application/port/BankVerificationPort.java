package com.atlashub.shared.application.port;

public interface BankVerificationPort {

    String verifyAccount(String bankCode, String accountNumber);
}
