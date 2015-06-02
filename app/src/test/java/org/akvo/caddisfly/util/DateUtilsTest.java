package org.akvo.caddisfly.util;

import junit.framework.TestCase;

import java.util.Calendar;

@SuppressWarnings("unused")
public class DateUtilsTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        ClassUtils.assertUtilityClassWellDefined(DateUtils.class);
    }

    public void testGetDaysNoDifference() throws Exception {
//        Calendar lastCheckDate = Calendar.getInstance();
//        lastCheckDate.setTimeInMillis(updateLastCheck);

        Calendar currentDate = Calendar.getInstance();
        int days = DateUtils.getDaysDifference(currentDate, currentDate);
        assertEquals(0, days);
    }

    public void testGetDaysDifferenceNull() throws Exception {
        Calendar currentDate = Calendar.getInstance();
        int days = DateUtils.getDaysDifference(currentDate, null);
        assertEquals(0, days);
    }

    public void testGetDaysDifference() throws Exception {
        Calendar currentDate = Calendar.getInstance();
        Calendar nextDate = Calendar.getInstance();
        nextDate.add(Calendar.DAY_OF_MONTH, 1);
        int days = DateUtils.getDaysDifference(currentDate, nextDate);
        assertEquals(1, days);
    }


}