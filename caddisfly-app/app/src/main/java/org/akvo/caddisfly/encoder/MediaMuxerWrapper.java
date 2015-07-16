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

package org.akvo.caddisfly.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MediaMuxerWrapper {
    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "MediaMuxerWrapper";

    private static final String DIR_NAME = "USBCameraTest";
    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
    private final MediaMuxer mMediaMuxer;    // API >= 18
    private String mOutputPath;
    private int mEncoderCount, mStartedCount;
    private boolean mIsStarted;

    /**
     * Constructor
     *
     * @param ext extension of output file
     * @throws IOException
     */
    public MediaMuxerWrapper(String ext) throws IOException {
        if (TextUtils.isEmpty(ext)) ext = ".mp4";
        try {
            mOutputPath = getCaptureFile(Environment.DIRECTORY_MOVIES, ext).toString();
        } catch (final NullPointerException e) {
            throw new RuntimeException("This app has no permission of writing external storage");
        }
        mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mEncoderCount = mStartedCount = 0;
        mIsStarted = false;
    }

    /**
     * generate output file
     *
     * @param type Environment.DIRECTORY_MOVIES etc.
     * @param ext  .mp4(.m4a for audio) or .png
     * @return return null when this app has no writing permission to external storage.
     */
    public static File getCaptureFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type), DIR_NAME);
        Log.d(TAG, "path=" + dir.toString());
        dir.mkdirs();
        if (dir.canWrite()) {
            return new File(dir, getDateTimeString() + ext);
        }
        return null;
    }

    /**
     * get current date and time as String
     *
     * @return
     */
    private static String getDateTimeString() {
        final GregorianCalendar now = new GregorianCalendar();
        return mDateTimeFormat.format(now.getTime());
    }

    public String getOutputPath() {
        return mOutputPath;
    }

//**********************************************************************
//**********************************************************************

    public void prepare() throws IOException {
    }

    public synchronized boolean isStarted() {
        return mIsStarted;
    }

    /**
     * request start recording from encoder
     *
     * @return true when muxer is ready to write
     */
    /*package*/
    synchronized boolean start() {
        if (DEBUG) Log.v(TAG, "start:");
        mStartedCount++;
        if ((mEncoderCount > 0) && (mStartedCount == mEncoderCount)) {
            mMediaMuxer.start();
            mIsStarted = true;
            notifyAll();
            if (DEBUG) Log.v(TAG, "MediaMuxer started:");
        }
        return mIsStarted;
    }

    /**
     * request stop recording from encoder when encoder received EOS
     */
	/*package*/
    synchronized void stop() {
        if (DEBUG) Log.v(TAG, "stop:mStartedCount=" + mStartedCount);
        mStartedCount--;
        if ((mEncoderCount > 0) && (mStartedCount <= 0)) {
            mMediaMuxer.stop();
            mIsStarted = false;
            if (DEBUG) Log.v(TAG, "MediaMuxer stopped:");
        }
    }

//**********************************************************************
//**********************************************************************

    /**
     * assign encoder to muxer
     *
     * @param format
     * @return minus value indicate error
     */
	/*package*/
    synchronized int addTrack(final MediaFormat format) {
        if (mIsStarted)
            throw new IllegalStateException("muxer already started");
        final int trackIx = mMediaMuxer.addTrack(format);
        if (DEBUG)
            Log.i(TAG, "addTrack:trackNum=" + mEncoderCount + ",trackIx=" + trackIx + ",format=" + format);
        return trackIx;
    }

    /**
     * write encoded data to muxer
     *
     * @param trackIndex
     * @param byteBuf
     * @param bufferInfo
     */
	/*package*/
    synchronized void writeSampleData(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo) {
        if (mStartedCount > 0)
            mMediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }

}
