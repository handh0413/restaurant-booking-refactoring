package com.cleancode.restaurant;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class BookingSchedulerTest {
    // ctrl + alt + c > introduce constant
    public static final Customer CUSTOMER = new Customer("Fake Name", "010-1111-2222");
    public static final int CAPACITY_PER_HOUR = 3;
    public static final int UNDER_CAPACITY = 1;
    public static final DateTime NOT_ON_THE_HOUR = new DateTime(2022, 02, 21, 9, 5);
    public static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("YYYY/MM/dd HH:mm");
    public static final DateTime ON_THE_HOUR = FORMAT.parseDateTime("2022/02/21 09:00");
    public BookingScheduler bookingScheduler;

    public BookingSchedulerTest() {
        bookingScheduler = new BookingScheduler(CAPACITY_PER_HOUR);
    }

    @Test(expected = RuntimeException.class)
    public void 예약은_정시에만_가능하다_정시가_아닌경우_예약불가() {
        // arrange
        Schedule schedule = new Schedule(NOT_ON_THE_HOUR, 1, CUSTOMER);

        // act
        bookingScheduler.addSchedule(schedule);

        // assert
        // expected runtime exception
    }

    @Test
    public void 예약은_정시에만_가능하다_정시인_경우_예약가능() {
        // arrange
        Schedule schedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        // act
        bookingScheduler.addSchedule(schedule);

        // assert
        assertThat(bookingScheduler.hasSchedule(schedule), is(true));
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대에_Capacity_초과할_경우_예외발생() {
        // arrange
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);
        bookingScheduler.addSchedule(schedule);

        try {
            // act
            Schedule newSchedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);
            bookingScheduler.addSchedule(schedule);
            fail();
        } catch (RuntimeException e) {
            // assert
            assertThat(e.getMessage(), is("Number of people is over restaurant capacity per hour"));
        }
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대가_다르면_Capacity_차있어도_스케쥴_추가_성공() {
        // arrange
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);
        bookingScheduler.addSchedule(schedule);

        // act
        DateTime differentHour = ON_THE_HOUR.plus(1);
        Schedule newSchedule = new Schedule(differentHour, UNDER_CAPACITY, CUSTOMER);
        bookingScheduler.addSchedule(newSchedule);

        // assert
        assertThat(bookingScheduler.hasSchedule(newSchedule), is(true));
    }

    @Test
    public void 예약완료시_SMS는_무조건_발송() {
        // arrange
        TestableSmsSender testableSmsSender = new TestableSmsSender();
        Schedule schedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);
        bookingScheduler.setSmsSender(testableSmsSender);

        // act
        bookingScheduler.addSchedule(schedule);

        // assert
        assertThat(testableSmsSender.isSendMethodIsCalled(), is(true));
    }

    @Test
    public void 이메일이_없는_경우에는_이메일_미발송() {

    }

    @Test
    public void 이메일이_있는_경우에는_이메일_발송() {

    }

    @Test
    public void 현재날짜가_일요일인_경우_예약불가_예외처리() {

    }

    @Test
    public void 현재날짜가_일요일이_아닌경우_예약가능() {

    }
}
