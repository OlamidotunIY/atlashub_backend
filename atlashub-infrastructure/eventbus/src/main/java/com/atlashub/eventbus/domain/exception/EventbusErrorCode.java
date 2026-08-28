package com.atlashub.eventbus.domain.exception;

import com.atlashub.shared.exception.ErrorCode;

public enum EventbusErrorCode implements ErrorCode {
    EVENT_NOT_FOUND,
    REPLAY_FAILED
}
