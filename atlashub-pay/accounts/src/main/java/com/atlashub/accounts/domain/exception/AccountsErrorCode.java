package com.atlashub.accounts.domain.exception;

import com.atlashub.shared.domain.exception.ErrorCode;

public enum AccountsErrorCode implements ErrorCode {
    INVALID_ACCOUNT_STATE,
    ACCOUNT_NOT_FOUND,
    UNSUPPORTED_COUNTRY,
    UNSUPPORTED_BANK
}
