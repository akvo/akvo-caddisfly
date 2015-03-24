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

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

import org.akvo.caddisfly.R;

public class SoundPoolPlayer {
    private final SparseIntArray mSounds = new SparseIntArray();
    private SoundPool mShortPlayer = null;

    public SoundPoolPlayer(Context pContext) {

        //noinspection deprecation
        this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_ALARM, 0);

        mSounds.put(R.raw.beep, this.mShortPlayer.load(pContext, R.raw.beep, 1));
        mSounds.put(R.raw.beep_long, this.mShortPlayer.load(pContext, R.raw.beep_long, 1));
        mSounds.put(R.raw.done, this.mShortPlayer.load(pContext, R.raw.done, 1));
        mSounds.put(R.raw.err, this.mShortPlayer.load(pContext, R.raw.err, 1));
    }

    public void playShortResource(int piResource) {
        int iSoundId = mSounds.get(piResource);
        this.mShortPlayer.play(iSoundId, 0.9f, 0.9f, 0, 0, 1);
    }

    public void release() {
        this.mShortPlayer.release();
        this.mShortPlayer = null;
    }
}