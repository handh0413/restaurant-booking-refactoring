package com.cleancode.restaurant;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class BookingScheduler {
    private int capacityPerHour;
    private List<Schedule> schedules;
    private SmsSender smsSender;
    private MailSender mailSender;

    public BookingScheduler(int capacityPerHour) {
        this.schedules = new ArrayList<>();
        this.capacityPerHour = capacityPerHour;
        this.smsSender = new SmsSender();
        this.mailSender = new MailSender();
    }

    public void addSchedule(Schedule schedule) {

        // 정각에 예약하지 않을 경우 RuntimeException 발생
        if (schedule.getDateTime().getMinuteOfHour() != 0) {
            throw new RuntimeException("Booking should be on the hour.");
        }

        // 시간당 예약 인원들 초과할 경우 RuntimeException 발생
        int numberOfPeople = schedule.getNumberOfPeople();
        for (Schedule bookedSchedule : schedules) {
            if (bookedSchedule.getDateTime().isEqual(schedule.getDateTime())) {
                numberOfPeople += bookedSchedule.getNumberOfPeople();
            }
        }

        if (numberOfPeople > capacityPerHour) {
            throw new RuntimeException("Number of people is over restaurant capacity per hour");
        }

        // 일요일에는 시스템을 오픈하지 않는다.
        DateTime now = getNow();
        if (now.getDayOfWeek() == DateTimeConstants.SUNDAY) {
            throw new RuntimeException("Booking system is not available on sunday");
        }

        schedules.add(schedule);

        // 고객에게 SMS 발송
        smsSender.send(schedule);
        // 고객이 Email을 가지고 있을 경우 Email 발송
        if (schedule.getCustomer().getEmail() != null) {
            mailSender.sendMail(schedule);
        }
    }

    public boolean hasSchedule(Schedule schedule) {
        return schedules.contains(schedule);
    }

    public void setSmsSender(SmsSender smsSender) {
        this.smsSender = smsSender;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public DateTime getNow() {
        // 일요일이 아닐 때...
        DateTimeFormatter format = DateTimeFormat.forPattern("YYYY/MM/dd HH:mm");
        DateTime sample = format.parseDateTime("2022/02/21 09:00");

        // 일요일 일 때
        // DateTimeFormatter format = DateTimeFormat.forPattern("YYYY/MM/dd HH:mm");
        // DateTime sample = format.parseDateTime("2022/02/20 09:00");
        return sample;
    }
}
