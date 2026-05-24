package com.leafall.yourtaxi.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public abstract class TimeUtils {
    public static long getCurrentTimeFromUTC() {
        return Instant.now().toEpochMilli();
    }
    public static long getEndOfDayMillis(ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);
        ZonedDateTime startOfNextDay = today.plusDays(1).atStartOfDay(zoneId);
        return startOfNextDay.toInstant().toEpochMilli() - 1;
    }
    public static long getStartOfDayMillis(ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);
        ZonedDateTime startOfDay = today.atStartOfDay(zoneId);
        return startOfDay.toInstant().toEpochMilli();
    }

    public static long getStartOfDayUTC() {
        return getStartOfDayMillis(ZoneId.of("UTC"));
    }

    public static long getEndOfDayUTC() {
        return getEndOfDayMillis(ZoneId.of("UTC"));
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
