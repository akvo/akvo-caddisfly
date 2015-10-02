package org.akvo.akvoqr.instructions_app;

import android.graphics.drawable.Drawable;

import org.json.JSONException;

/**
 * Created by linda on 9/21/15.
 */
public interface InstructionsListener {

    String getInstruction(int id) throws JSONException;

    Drawable getInstructionRes(int id) throws JSONException;
}
