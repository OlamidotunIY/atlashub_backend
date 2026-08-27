package com.atlashub.shared.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppLogger {
    private AppLogger() {}

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
