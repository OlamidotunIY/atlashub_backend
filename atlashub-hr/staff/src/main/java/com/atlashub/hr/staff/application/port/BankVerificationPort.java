package com.atlashub.hr.staff.application.port;

public interface BankVerificationPort {

    String verifyAccount(String bankCode, String accountNumber);
}
