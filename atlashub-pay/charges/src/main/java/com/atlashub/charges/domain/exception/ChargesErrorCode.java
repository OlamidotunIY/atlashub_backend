package com.atlashub.charges.domain.exception;

import com.atlashub.shared.domain.exception.ErrorCode;

public enum ChargesErrorCode implements ErrorCode {
    UNKNOWN_EVENT_TYPE,
    CHARGE_NOT_FOUND,
    INVALID_CHARGE_STATE,
    EXTERNAL_SERVICE_ERROR
}
