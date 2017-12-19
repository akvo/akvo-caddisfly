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

package org.akvo.caddisfly.helper;

import android.app.Activity;
import android.content.Intent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.chamber.ChamberTestActivity;
import org.akvo.caddisfly.util.AlertUtil;

public class ErrorMessages {

    private static final String MESSAGE_TWO_LINE_FORMAT = "%s%n%n%s";
    private static final String TWO_SENTENCE_FORMAT = "%s%n%n%s";

    /**
     * Error message for feature not supported by the device.
     */
    public static void alertFeatureNotSupported(Activity activity, boolean finishOnDismiss) {
        String message = String.format(MESSAGE_TWO_LINE_FORMAT, activity.getString(R.string.phoneDoesNotSupport),
                activity.getString(R.string.pleaseContactSupport));

        AlertUtil.showAlert(activity, R.string.notSupported, message,
                R.string.ok,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (finishOnDismiss) {
                        activity.finish();
                    }
                }, null,
                dialogInterface -> {
                    dialogInterface.dismiss();
                    if (finishOnDismiss) {
                        activity.finish();
                    }
                }
        );
    }

    /**
     * Error message for configuration not loading correctly.
     */
    public static void alertCouldNotLoadConfig(Activity activity) {
        String message = String.format(TWO_SENTENCE_FORMAT,
                activity.getString(R.string.errorLoadingConfiguration),
                activity.getString(R.string.pleaseContactSupport));
        AlertUtil.showError(activity, R.string.error, message, null, R.string.ok,
                (dialogInterface, i) -> dialogInterface.dismiss(), null, null);
    }

    /**
     * Alert message for calibration incomplete or invalid.
     */
    public static void alertCalibrationIncomplete(Activity activity, TestInfo testInfo) {

        String message = activity.getString(R.string.errorCalibrationIncomplete, testInfo.getName());
        message = String.format(MESSAGE_TWO_LINE_FORMAT, message,
                activity.getString(R.string.doYouWantToCalibrate));

        AlertUtil.showAlert(activity, R.string.cannotStartTest, message, R.string.calibrate,
                (dialogInterface, i) -> {

                    final Intent intent = new Intent(activity, ChamberTestActivity.class);
                    intent.putExtra(ConstantKey.TEST_INFO, testInfo);
                    activity.startActivity(intent);

                    activity.setResult(Activity.RESULT_CANCELED);
                    dialogInterface.dismiss();
//                    activity.finish();
                }, (dialogInterface, i) -> {
                    activity.setResult(Activity.RESULT_CANCELED);
                    dialogInterface.dismiss();
                    activity.finish();
                },
                dialogInterface -> {
                    activity.setResult(Activity.RESULT_CANCELED);
                    dialogInterface.dismiss();
                    activity.finish();
                }
        );
    }

    public static void alertCalibrationExpired(Activity activity) {

        String message = String.format(MESSAGE_TWO_LINE_FORMAT,
                activity.getString(R.string.errorCalibrationExpired),
                activity.getString(R.string.orderFreshBatch));

        AlertUtil.showAlert(activity, R.string.cannotStartTest,
                message, R.string.ok,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    activity.finish();
                }, null,
                dialogInterface -> {
                    dialogInterface.dismiss();
                    activity.finish();
                }
        );
    }

}
