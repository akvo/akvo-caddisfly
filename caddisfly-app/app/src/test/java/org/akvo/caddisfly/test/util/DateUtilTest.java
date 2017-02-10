/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.test.util;

import org.akvo.caddisfly.util.DateUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unused")
public class DateUtilTest {

    @Before
    public void setUp() throws Exception {
        ClassUtil.assertUtilityClassWellDefined(DateUtil.class);
    }

    @Test
    public void testGetDaysNoDifference() {
        Calendar currentDate = Calendar.getInstance();
        int days = DateUtil.getDaysDifference(currentDate, currentDate);
        assertEquals(0, days);
    }

    @Test
    public void testGetDaysDifferenceNull() {
        Calendar currentDate = Calendar.getInstance();
        int days = DateUtil.getDaysDifference(currentDate, null);
        assertEquals(0, days);
    }

    @Test
    public void testGetDaysDifference() {
        Calendar currentDate = Calendar.getInstance();
        Calendar nextDate = Calendar.getInstance();
        nextDate.add(Calendar.DAY_OF_MONTH, 1);
        int days = DateUtil.getDaysDifference(currentDate, nextDate);
        assertEquals(1, days);
    }

    @Test
    public void testGetHoursDifference() {
        Calendar currentDate = Calendar.getInstance();
        Calendar nextDate = Calendar.getInstance();
        nextDate.add(Calendar.HOUR_OF_DAY, 2);
        int hours = DateUtil.getHoursDifference(currentDate, nextDate);
        assertEquals(2, hours);
    }

}