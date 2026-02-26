package com.leafall.yourtaxi.utils;

import java.time.Instant;
import java.util.Date;

public abstract class TimeUtils {
    public static long getCurrentTimeFromUTC() {
        return Instant.now().toEpochMilli();
    }

    public static Date getExpiredDateFromUTC(long minutesToExpire) {
        var time = getSecondFromMinutes(minutesToExpire);
        var instant = Instant.now().plusSeconds(time);
        return Date.from(instant);
    }

    private static long getSecondFromMinutes(long minutes) {
        return minutes * 60;
    }
}
