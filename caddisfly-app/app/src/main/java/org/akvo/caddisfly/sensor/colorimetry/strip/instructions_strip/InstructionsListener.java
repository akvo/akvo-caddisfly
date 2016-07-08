package org.akvo.caddisfly.sensor.colorimetry.strip.instructions_strip;

import android.graphics.drawable.Drawable;

import org.json.JSONException;

/**
 * Created by linda on 9/21/15
 */
interface InstructionsListener {

    String getInstruction(int id) throws JSONException;

    Drawable getInstructionDrawable(int id) throws JSONException;
}
