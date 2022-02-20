package com.cleancode.restaurant;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SundayBookingScheduler extends BookingScheduler {
    public SundayBookingScheduler(int capacityPerHour) {
        super(capacityPerHour);
    }

    @Override
    public DateTime getNow() {
        DateTimeFormatter format = DateTimeFormat.forPattern("YYYY/MM/dd HH:mm");
        DateTime sundayOnTheHour = format.parseDateTime("2022/02/21 09:00");
        return sundayOnTheHour;
    }
}
