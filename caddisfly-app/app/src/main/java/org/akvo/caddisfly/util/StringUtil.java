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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.widget.CenteredImageSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {

    private StringUtil() {
    }

    public static Spanned getStringResourceByName(Context context, String theKey) {
        String key = theKey.trim();
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

    public static SpannableStringBuilder toInstruction(AppCompatActivity context, TestInfo testInfo, String text) {

        SpannableStringBuilder builder = new SpannableStringBuilder();

        Spanned spanned = StringUtil.getStringResourceByName(context, text);
        builder.append(spanned);

        Matcher m = Pattern.compile("\\(\\*(\\w+)\\*\\)").matcher(builder);

        while (m.find()) {

            int resId = context.getResources().getIdentifier("button_" + m.group(1),
                    "drawable", context.getPackageName());

            if (resId > 0) {
                builder.setSpan(new CenteredImageSpan(context, resId),
                        m.start(0), m.end(0), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }

        // Set reagent in the string
        replaceReagentTags(testInfo, builder);

        // Set sample quantity in the string
        Matcher m1 = Pattern.compile("%sampleQuantity").matcher(builder);
        while (m1.find()) {
            builder.replace(m1.start(), m1.end(), testInfo.getSampleQuantity());
        }

        // Set reaction time in the string
        for (int i = 1; i < 5; i++) {
            Matcher m2 = Pattern.compile("%reactionTime" + i).matcher(builder);
            while (m2.find()) {
                builder.replace(m2.start(), m2.end(), testInfo.getReagent(i - 1).reactionTime.toString());
            }
        }

        insertDialogLinks(context, builder);

        return builder;
    }

    private static void insertDialogLinks(AppCompatActivity context, SpannableStringBuilder builder) {
        if (builder.toString().contains("[a topic=")) {

            int startIndex = builder.toString().indexOf("[a topic=");

            String topic;
            Pattern p = Pattern.compile("\\[a topic=(.*?)]");
            Matcher m3 = p.matcher(builder);
            if (m3.find()) {
                topic = m3.group(1);
                builder.replace(m3.start(), m3.end(), "");
                int endIndex = builder.toString().indexOf("[/a]");
                builder.replace(endIndex, endIndex + 4, "");

                String finalTopic = topic;
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        if (finalTopic.equalsIgnoreCase("sulfide")) {
                            DialogFragment newFragment = new SulfideDialogFragment();
                            newFragment.show(context.getSupportFragmentManager(), "sulfideDialog");
                        } else {
                            DialogFragment newFragment = new DilutionDialogFragment();
                            newFragment.show(context.getSupportFragmentManager(), "dilutionDialog");
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };
                builder.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), startIndex, endIndex, 0);
            }
        }
    }

    private static void replaceReagentTags(TestInfo testInfo, SpannableStringBuilder builder) {
        for (int i = 1; i < 5; i++) {
            Matcher m1 = Pattern.compile("%reagent" + i).matcher(builder);
            while (m1.find()) {
                String name = testInfo.getReagent(i - 1).name;
                String code = testInfo.getReagent(i - 1).code;
                if (!code.isEmpty()) {
                    name = String.format("%s (%s)", name, code);
                }
                builder.replace(m1.start(), m1.end(), name);
            }
        }
    }

    public static String convertToTags(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append("(*").append(text.charAt(i)).append("*)");
        }
        return result.toString();
    }

    public static String getStringByName(Context context, String name) {
        if (name == null) {
            return "";
        }
        return context.getResources().getString(context.getResources()
                .getIdentifier(name, "string", context.getPackageName()));
    }

    public static class SulfideDialogFragment extends DialogFragment {
        @NonNull
        @SuppressLint("InflateParams")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_sulfide_instruction, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());

            return builder.create();
        }
    }

    public static class DilutionDialogFragment extends DialogFragment {
        @NonNull
        @SuppressLint("InflateParams")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_dilution_instruction, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());


            return builder.create();
        }
    }

}
