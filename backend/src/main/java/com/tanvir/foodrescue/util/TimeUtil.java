package com.tanvir.foodrescue.util;

import java.time.LocalTime;

/** All times in the system are "minutes from midnight". Past midnight keeps counting (24:30 = 12:30 AM). */
public final class TimeUtil {

    private TimeUtil() {}

    public static String format(int minutes) {
        int m = ((minutes % 1440) + 1440) % 1440;
        int h = m / 60;
        int mm = m % 60;
        String ampm = h >= 12 ? "PM" : "AM";
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return String.format("%d:%02d %s", h12, mm, ampm);
    }

    /** Parses "HH:mm" (24h). Times before 05:00 are treated as after-midnight, so they sort after 23:59. */
    public static int parseHHmm(String text) {
        String[] p = text.trim().split(":");
        if (p.length != 2) throw new IllegalArgumentException("Time must look like 21:45");
        int h = Integer.parseInt(p[0].trim());
        int m = Integer.parseInt(p[1].trim());
        if (h < 0 || h > 23 || m < 0 || m > 59) throw new IllegalArgumentException("Time must look like 21:45");
        int minutes = h * 60 + m;
        return h < 5 ? minutes + 1440 : minutes;
    }

    public static int nowMinutes() {
        LocalTime t = LocalTime.now();
        int minutes = t.getHour() * 60 + t.getMinute();
        return t.getHour() < 5 ? minutes + 1440 : minutes;
    }
}
