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

import java.util.Calendar;

/**
 * Utility functions for date and time
 */
public final class DateUtils {

    private DateUtils() {
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

        return (int) ((calendar2.getTimeInMillis() -
                calendar1.getTimeInMillis()) / (1000 * 60 * 60 * 24));
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

        return (int) ((calendar2.getTimeInMillis() -
                calendar1.getTimeInMillis()) / (1000 * 60 * 60));
    }

}
