package org.akvo.caddisfly.util;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;

import org.akvo.caddisfly.R;

public class PreferenceCategoryCustom extends PreferenceCategory {

    public PreferenceCategoryCustom(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_category);
    }

    public PreferenceCategoryCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_category);
    }
}