package com.atlashub.authentication.domain.exceptions;

import com.atlashub.shared.domain.exception.AuthorizationException;

public class LiveEnvironmentUnavailableException extends AuthorizationException {
    public LiveEnvironmentUnavailableException() {
        super("Live mode requires approved AtlasHub compliance");
    }
}
