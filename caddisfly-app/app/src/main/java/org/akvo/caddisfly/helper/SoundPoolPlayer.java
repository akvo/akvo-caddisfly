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

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.annotation.RawRes;
import android.util.SparseIntArray;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.preference.AppPreferences;

/**
 * Manages various sounds used in the app
 */
public class SoundPoolPlayer {
    private final SparseIntArray mSounds = new SparseIntArray();
    private final Context mContext;
    private SoundPool mPlayer = null;

    public SoundPoolPlayer(Context context) {
        mContext = context;
        setupPlayer(mContext);
    }

    private void setupPlayer(Context context) {
        //noinspection deprecation
        mPlayer = new SoundPool(4, AudioManager.STREAM_ALARM, 0);

        //beep sound for every photo taken during a test
        mSounds.put(R.raw.beep, this.mPlayer.load(context, R.raw.beep, 1));

        //long beep sound if the contamination in the water sample is very high
        mSounds.put(R.raw.beep_long, this.mPlayer.load(context, R.raw.beep_long, 1));

        //done sound when the test completes successfully
        mSounds.put(R.raw.done, this.mPlayer.load(context, R.raw.done, 1));

        //error sound when the test fails
        mSounds.put(R.raw.err, this.mPlayer.load(context, R.raw.err, 1));

        //low beep sound
        mSounds.put(R.raw.futurebeep2, this.mPlayer.load(context, R.raw.futurebeep2, 1));

    }
    /**
     * Play a short sound effect
     *
     * @param resourceId the
     */
    public void playShortResource(@RawRes int resourceId) {

        if (mPlayer == null) {
            setupPlayer(mContext);
        }

        //play sound if the sound is not turned off in the preference
        if (!AppPreferences.isSoundOff()) {
            mPlayer.play(mSounds.get(resourceId), AppConfig.SOUND_EFFECTS_VOLUME,
                    AppConfig.SOUND_EFFECTS_VOLUME, 0, 0, 1);
        }
    }

    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
