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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.os.AsyncTask;

import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.detector.FinderPatternInfo;
import org.akvo.caddisfly.util.detector.FinderPatternInfoToJson;

/**
 * Created by linda on 11/19/15
 */
class StoreDataTask extends AsyncTask<Void, Void, Boolean> {

    private final int imageCount;
    private final byte[] data;
    private final FinderPatternInfo info;
    private final CameraViewListener listener;
    private final Context context;

    StoreDataTask(Context listener,
                  int imageCount, byte[] data, FinderPatternInfo info) {

        this.listener = (CameraViewListener) listener;
        this.imageCount = imageCount;
        this.data = data == null ? null : data.clone();
        this.info = info;
        this.context = listener;

    }

    // The data here is still in the original YUV preview format.
    @Override
    protected Boolean doInBackground(Void... params) {
        FileUtil.writeByteArray(context, data, Constant.DATA + imageCount);
        String json = FinderPatternInfoToJson.toJson(info);
        FileUtil.writeToInternalStorage(context, Constant.INFO + imageCount, json);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean written) {
        listener.dataSent();
    }
}
