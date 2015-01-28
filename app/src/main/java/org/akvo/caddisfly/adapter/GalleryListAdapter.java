/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.ImageUtils;
import org.akvo.caddisfly.util.PreferencesUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GalleryListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;

    private final Activity mActivity;

    private final Context mContext;

    private final long mTestId;

    private final String mTestType;

    private final boolean mShowResult;

    private ArrayList<String> mFilePaths = new ArrayList<>();

    @SuppressWarnings("SameParameterValue")
    public GalleryListAdapter(Activity activity, String testType, long testId,
                              ArrayList<String> filePaths, boolean showResult) {
        mContext = activity;
        mInflater = activity.getLayoutInflater();
        mFilePaths = filePaths;
        mActivity = activity;
        mTestId = testId;
        mTestType = testType;
        mShowResult = showResult;
    }

    /*
     * Resizing image size
     */
    private static Bitmap decodeFile(String filePath) {
        return ImageUtils.decodeFile(filePath);
    }

    @Override
    public int getCount() {
        return Math.max(1, this.mFilePaths.size());
    }

    @Override
    public Object getItem(int position) {
        return this.mFilePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        if (mFilePaths.size() <= position) {
            if (view == null) {
                view = new View(mContext);
            }
            return view;
        }
        ViewHolder holder;
        final String file = mFilePaths.get(position);

        if (view == null) {
            view = mInflater.inflate(R.layout.row_gallery, parent, false);
            holder = new ViewHolder();
            holder.serialNumber = (TextView) view.findViewById(R.id.serialNumberText);
            holder.icon = (ImageView) view.findViewById(R.id.photoImageView);
            holder.timestamp = (TextView) view.findViewById(R.id.dateText);
            holder.result = (TextView) view.findViewById(R.id.resultText);
            holder.brightness = (TextView) view.findViewById(R.id.brightnessText);
            holder.progress = (ProgressBar) view.findViewById(R.id.imageProgressBar);

            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.position = position;

        // Using an AsyncTask to load the slow images in a background thread
        (new AsyncTask<ViewHolder, Void, Bitmap>() {
            private ViewHolder v;

            @Override
            protected Bitmap doInBackground(ViewHolder... params) {
                v = params[0];

                if (!file.isEmpty()) {
                    return decodeFile(file);
                }
                return null;
            }

            @SuppressWarnings("ResourceType")
            @SuppressLint("SimpleDateFormat") // Using SimpleDateFormat to display seconds also
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (v.position == position) {
                    v.serialNumber.setText(String.valueOf(position + 1));
                    v.progress.setVisibility(View.GONE);
                    v.icon.setVisibility(View.VISIBLE);
                    v.icon.setImageBitmap(bitmap);
                    //v.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Pattern pattern = Pattern
                            .compile("pic-(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})-s");
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
                        boolean is24HourFormat = android.text.format.DateFormat
                                .is24HourFormat(mActivity);
                        String timePattern = mActivity.getString(
                                is24HourFormat ? R.string.twentyFourHourTime
                                        : R.string.twelveHourTime
                        );
                        DateFormat tf = new SimpleDateFormat(timePattern);
                        v.timestamp.setText(tf.format(cal.getTime()));

                        double result;
                        result = PreferencesUtils.getDouble(mContext,
                                String.format(mContext.getString(R.string.resultValueKey),
                                        mTestType, mTestId, position)
                        );
                        int color = PreferencesUtils.getInt(mContext,
                                String.format(mContext.getString(R.string.resultColorKey),
                                        mTestType, mTestId,
                                        position), -2
                        );

                        if (color != -2) {
                            v.brightness.setText(String.format("Brightness: %d", ColorUtils.getBrightness(color)));
                        }

                        int quality = PreferencesUtils.getInt(mContext,
                                String.format(mContext.getString(R.string.resultQualityKey),
                                        mTestType, mTestId,
                                        position), 0
                        );
                        if (color == -2) {
                            v.result.setText("");
                        } else if (color == -1) {
                            v.result.setText(R.string.error);
                        } else {
                            if (mShowResult) {
                                if (result < 0) {
                                    v.result.setText(R.string.outOfRange);
                                } else {
                                    v.result.setText(
                                            String.format("%.1f (%d%%) rgb:%s", result, quality,
                                                    ColorUtils.getColorRgbString(color))
                                    );
                                }
                            } else {
                                v.result.setText(
                                        String.format("(%d%%) rgb:%s", quality,
                                                ColorUtils.getColorRgbString(color))
                                );
                            }
                        }

/*
                        if (result < 0) {
                            v.color.setText(R.string.error);
                        } else {
                            v.color.setText(String.format("rgb: %s", ColorUtils.getColorRgbString(color)));
                        }


                        if (quality < 0) {
                            v.quality.setText(R.string.error);
                        } else {
                            v.quality.setText(String.format("quality: %d%%", quality));
                        }
*/

                    }
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
        return view;
    }

    static class ViewHolder {

        TextView serialNumber;

        TextView timestamp;

        TextView result;

        TextView brightness;

        //TextView quality;

        ImageView icon;

        ProgressBar progress;

        int position;

    }
}
