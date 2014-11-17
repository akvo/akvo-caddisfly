package org.akvo.caddisfly.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.SparseIntArray;

import org.akvo.caddisfly.R;

public class SoundPoolPlayer {
    private final SparseIntArray mSounds = new SparseIntArray();
    private SoundPool mShortPlayer = null;

    public SoundPoolPlayer(Context pContext) {

        if (Build.VERSION.SDK_INT > 20) {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            this.mShortPlayer = new SoundPool.Builder().setAudioAttributes(attr).setMaxStreams(4).build();
        } else {
            //noinspection deprecation
            this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_ALARM, 0);
        }

        mSounds.put(R.raw.beep, this.mShortPlayer.load(pContext, R.raw.beep, 1));
        mSounds.put(R.raw.beep_long, this.mShortPlayer.load(pContext, R.raw.beep_long, 1));
        mSounds.put(R.raw.done, this.mShortPlayer.load(pContext, R.raw.done, 1));
        mSounds.put(R.raw.err, this.mShortPlayer.load(pContext, R.raw.err, 1));
    }

    public void playShortResource(int piResource) {
        int iSoundId = mSounds.get(piResource);
        this.mShortPlayer.play(iSoundId, 0.99f, 0.99f, 0, 0, 1);
    }

    public void release() {
        this.mShortPlayer.release();
        this.mShortPlayer = null;
    }
}