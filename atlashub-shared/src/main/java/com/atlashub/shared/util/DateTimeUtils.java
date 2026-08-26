package com.atlashub.shared.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class DateTimeUtils {
    
    private DateTimeUtils() {}
    
    /**
     * Returns the current date and time in UTC.
     * All timestamps in AtlasHub should be generated using this method.
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
}
