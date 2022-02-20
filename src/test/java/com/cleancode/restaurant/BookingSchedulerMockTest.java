package com.cleancode.restaurant;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BookingSchedulerMockTest {
    // ctrl + alt + c > introduce constant
    public static final Customer CUSTOMER = Mockito.mock(Customer.class);
    public static final Customer CUSTOMER_WITH_MAIL = Mockito.mock(Customer.class, Mockito.RETURNS_MOCKS);

    public static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("YYYY/MM/dd HH:mm");
    public static final DateTime ON_THE_HOUR = FORMAT.parseDateTime("2022/02/21 09:00");
    public static final DateTime NOT_ON_THE_HOUR = new DateTime(2022, 02, 21, 9, 5);

    public static final int UNDER_CAPACITY = 1;
    public static final int CAPACITY_PER_HOUR = 3;

    @InjectMocks
    @Spy
    public BookingScheduler bookingScheduler;

    @Mock
    public SmsSender smsSender = new SmsSender();

    @Mock
    public MailSender mailSender = new MailSender();

    public BookingSchedulerMockTest() {
        bookingScheduler = new BookingScheduler(CAPACITY_PER_HOUR);
    }

    @Before
    public void setUp() {

    }

    @Test
    public void test_mock() {
        List mockList = Mockito.mock(ArrayList.class);

        mockList.add("one");
        verify(mockList, times(1)).add("one");
        assertEquals(0, mockList.size());

        Mockito.when(mockList.size()).thenReturn(100);
        assertEquals(100, mockList.size());
    }

    @Test
    public void test_spy() {
        List spyList = Mockito.spy(new ArrayList<String>());

        spyList.add("one");
        System.out.println(spyList.size());

        spyList.add("two");
        System.out.println(spyList.size());

        verify(spyList, times(1)).add("one"); // 과거에 호출한 것을 검증
        // Mockito.verify(spyList).add("three"); // 과거에 호출하지 않아서 오류 발생

        Mockito.when(spyList.size()).thenReturn(100);
        System.out.println(spyList.size());
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
        Schedule schedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        // act
        bookingScheduler.addSchedule(schedule);

        // assert
        verify(smsSender, times(1)).send(schedule);
    }

    @Test
    public void 이메일이_없는_경우에는_이메일_미발송() {
        // arrange
       Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);

        // act
        bookingScheduler.addSchedule(schedule);

        // assert
        verify(mailSender, never()).sendMail(any(Schedule.class));
    }

    @Test
    public void 이메일이_있는_경우에는_이메일_발송() {
        // arrange
        Schedule schedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER_WITH_MAIL);

        // act
        bookingScheduler.addSchedule(schedule);

        // assert
        verify(mailSender, times(1)).sendMail(any(Schedule.class));
    }

    @Test
    public void 현재날짜가_일요일인_경우_예약불가_예외처리() {
        // arrange
        DateTime sunday = DateTimeFormat.forPattern("YYYY/MM/dd HH:mm").parseDateTime("2022/02/20 09:00");
        when(bookingScheduler.getNow()).thenReturn(sunday);

        try {
            Schedule newSchedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER_WITH_MAIL);
            bookingScheduler.addSchedule(newSchedule);
            fail();
        } catch (RuntimeException e) {
            // assert
            assertThat(e.getMessage(), is("Booking system is not available on sunday"));
        }
    }

    @Test
    public void 현재날짜가_일요일이_아닌경우_예약가능() {
        // arrange
        DateTime monday = DateTimeFormat.forPattern("YYYY/MM/dd HH:mm").parseDateTime("2022/02/21 09:00");
        when(bookingScheduler.getNow()).thenReturn(monday);

        Schedule newSchedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER_WITH_MAIL);
        bookingScheduler.addSchedule(newSchedule);

        // assert
        assertThat(bookingScheduler.hasSchedule(newSchedule), is(true));
    }
}
