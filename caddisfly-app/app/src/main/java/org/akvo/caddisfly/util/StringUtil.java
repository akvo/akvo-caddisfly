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

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.widget.CenteredImageSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {

    private StringUtil() {
    }

    public static Spanned getStringResourceByName(Context context, String key) {
        key = key.trim();
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(key, "string", packageName);
        if (resId == 0) {
            return Spannable.Factory.getInstance().newSpannable(fromHtml(key));
        } else {
            return Spannable.Factory.getInstance().newSpannable(context.getText(resId));
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public static SpannableStringBuilder toInstruction(Context context, TestInfo testInfo, String text) {

        SpannableStringBuilder builder = new SpannableStringBuilder();

        Spanned spanned = StringUtil.getStringResourceByName(context, text);
        builder.append(spanned);

        Matcher m = Pattern.compile("#(\\w+)#").matcher(builder);

        while (m.find()) {

            int resId = context.getResources().getIdentifier("button_" + m.group(1), "drawable", context.getPackageName());

            if (resId > 0) {
                builder.setSpan(new CenteredImageSpan(context, resId),
                        m.start(1) - 1, m.end(1) + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }

        // Set reagent in the string
        for (int i = 1; i < 5; i++) {
            Matcher m1 = Pattern.compile("%reagent" + i).matcher(builder);
            while (m1.find()) {
                builder.replace(m1.start(), m1.end(), testInfo.getReagent(i - 1));
            }
        }

        // Set sample quantity in the string
        Matcher m1 = Pattern.compile("%samplequantity").matcher(builder);
        while (m1.find()) {
            builder.replace(m1.start(), m1.end(), testInfo.getSampleQuantity().toString());
        }

        // Set reaction time in the string
        for (int i = 1; i < 5; i++) {
            Matcher m2 = Pattern.compile("%reactiontime" + i).matcher(builder);
            while (m2.find()) {
                builder.replace(m2.start(), m2.end(), testInfo.getReactionTime(i - 1));
            }
        }

        return builder;
    }

    public static String convertToTags(String text) {
        String result = "";
        for (int i = 0; i < text.length(); i++) {
            result += "#";
            result += text.charAt(i);
            result += "#";
        }
        return result;
    }

    public static String getStringByName(Context context, String name) {
        return context.getResources().getString(context.getResources().getIdentifier(name, "string", context.getPackageName()));
    }

}
