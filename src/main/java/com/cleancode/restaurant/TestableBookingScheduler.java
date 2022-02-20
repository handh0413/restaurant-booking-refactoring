package com.cleancode.restaurant;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class TestableBookingScheduler extends BookingScheduler {
    private String dateTime;

    public TestableBookingScheduler(int capacityPerHour, String dateTime) {
        super(capacityPerHour);
        this.dateTime = dateTime;
    }

    @Override
    public DateTime getNow() {
        return DateTimeFormat.forPattern("YYYY/MM/dd HH:mm").parseDateTime(dateTime);
    }
}
