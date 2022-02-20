package com.cleancode.restaurant;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MondayBookingScheduler extends BookingScheduler {
    public MondayBookingScheduler(int capacityPerHour) {
        super(capacityPerHour);
    }

    @Override
    public DateTime getNow() {
        DateTimeFormatter format = DateTimeFormat.forPattern("YYYY/MM/dd HH:mm");
        DateTime sundayOnTheHour = format.parseDateTime("2022/02/21 17:00");
        return sundayOnTheHour;
    }
}
