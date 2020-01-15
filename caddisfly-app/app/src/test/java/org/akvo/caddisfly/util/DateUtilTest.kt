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

package org.akvo.caddisfly.util

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [Build.VERSION_CODES.P])
class DateUtilTest {

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ClassUtil.assertUtilityClassWellDefined(DateUtil::class.java)
    }

    @Test
    fun testGetDaysNoDifference() {
        val currentDate = Calendar.getInstance()
        val days = DateUtil.getDaysDifference(currentDate, currentDate)
        assertEquals(0, days.toLong())
    }

    @Test
    fun testGetDaysDifferenceNull() {
        val currentDate = Calendar.getInstance()
        val days = DateUtil.getDaysDifference(currentDate, null)
        assertEquals(0, days.toLong())
    }

    @Test
    fun testGetDaysDifference() {
        val currentDate = Calendar.getInstance()
        val nextDate = Calendar.getInstance()
        nextDate.add(Calendar.DAY_OF_MONTH, 1)
        val days = DateUtil.getDaysDifference(currentDate, nextDate)
        assertEquals(1, days.toLong())
    }

    @Test
    fun testGetHoursDifference() {
        val currentDate = Calendar.getInstance()
        val nextDate = Calendar.getInstance()
        nextDate.add(Calendar.HOUR_OF_DAY, 2)
        val hours = DateUtil.getHoursDifference(currentDate, nextDate)
        assertEquals(2, hours.toLong())
    }

}