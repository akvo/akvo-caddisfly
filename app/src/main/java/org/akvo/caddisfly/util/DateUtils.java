/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import java.util.Calendar;

public class DateUtils {

    private DateUtils() {
    }

    public static int getDaysDifference(Calendar calendar1, Calendar calendar2) {
        if (calendar1 == null || calendar2 == null) {
            return 0;
        }

        return (int) ((calendar2.getTimeInMillis() - calendar1.getTimeInMillis()) / (1000 * 60 * 60
                * 24));
    }

   /* public static long getDateFromFilename(String file) {
        Pattern pattern = Pattern
                .compile("pic-(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})");
        Matcher matcher = pattern.matcher(file);
        if (matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2)) - 1;
            int day = Integer.parseInt(matcher.group(3));
            int hour = Integer.parseInt(matcher.group(4));
            int minute = Integer.parseInt(matcher.group(5));
            int second = Integer.parseInt(matcher.group(6));

            Calendar cal = Calendar.getInstance();
            //noinspection MagicConstant
            cal.set(year, month, day, hour, minute, second);
            return cal.getTimeInMillis();
        }
        return 0;
    }
*/
}
