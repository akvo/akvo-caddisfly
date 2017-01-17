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

package org.akvo.caddisfly.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility functions for date and time
 */
public final class DateUtil {

    private static final String TAG = "DateUtil";

    private DateUtil() {
    }

    /**
     * Gets the number of days in between two given dates
     *
     * @param calendar1 the first date
     * @param calendar2 the second date
     * @return the number days
     */
    public static int getDaysDifference(Calendar calendar1, Calendar calendar2) {
        if (calendar1 == null || calendar2 == null) {
            return 0;
        }

        return (int) ((calendar2.getTimeInMillis()
                - calendar1.getTimeInMillis()) / (1000 * 60 * 60 * 24));
    }

    /**
     * Gets the number of hours in between two given dates
     *
     * @param calendar1 the first date
     * @param calendar2 the second date
     * @return the number hours
     */
    public static int getHoursDifference(Calendar calendar1, Calendar calendar2) {
        if (calendar1 == null || calendar2 == null) {
            return 0;
        }

        return (int) ((calendar2.getTimeInMillis()
                - calendar1.getTimeInMillis()) / (1000 * 60 * 60));
    }


//    public static String getDateTimeString() {
//        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
//    }


    public static Date convertStringToDate(String dateString, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
        try {
            return simpleDateFormat.parse(dateString.trim());
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
