/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import junit.framework.TestCase;

import java.util.Calendar;

@SuppressWarnings("unused")
public class DateUtilTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        ClassUtil.assertUtilityClassWellDefined(DateUtil.class);
    }

    public void testGetDaysNoDifference() throws Exception {
        Calendar currentDate = Calendar.getInstance();
        int days = DateUtil.getDaysDifference(currentDate, currentDate);
        assertEquals(0, days);
    }

    public void testGetDaysDifferenceNull() throws Exception {
        Calendar currentDate = Calendar.getInstance();
        int days = DateUtil.getDaysDifference(currentDate, null);
        assertEquals(0, days);
    }

    public void testGetDaysDifference() throws Exception {
        Calendar currentDate = Calendar.getInstance();
        Calendar nextDate = Calendar.getInstance();
        nextDate.add(Calendar.DAY_OF_MONTH, 1);
        int days = DateUtil.getDaysDifference(currentDate, nextDate);
        assertEquals(1, days);
    }

    public void testGetHoursDifference() throws Exception {
        Calendar currentDate = Calendar.getInstance();
        Calendar nextDate = Calendar.getInstance();
        nextDate.add(Calendar.HOUR_OF_DAY, 2);
        int hours = DateUtil.getHoursDifference(currentDate, nextDate);
        assertEquals(2, hours);
    }


}