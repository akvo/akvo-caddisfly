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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;

/**
 * Various utility functions to get/set values from/to SharedPreferences
 */
public class PreferencesUtil {

    private PreferencesUtil() {
    }

    /**
     * Gets a preference key from strings
     *
     * @param context the context
     * @param keyId   the key id
     * @return the string key
     */
    private static String getKey(Context context, @StringRes int keyId) {
        return context.getString(keyId);
    }

    /**
     * Gets a boolean value from preferences
     *
     * @param context      the context
     * @param keyId        the key id
     * @param defaultValue the default value
     * @return the stored boolean value
     */
    @SuppressWarnings("SameParameterValue")
    public static boolean getBoolean(Context context, @StringRes int keyId, boolean defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(getKey(context, keyId), defaultValue);
    }

    /**
     * Sets a boolean value to preferences
     *
     * @param context the context
     * @param keyId   the key id
     * @param value   the value
     */
    @SuppressWarnings("SameParameterValue")
    public static void setBoolean(Context context, @StringRes int keyId, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(getKey(context, keyId), value);
        editor.apply();

    }

    /**
     * Gets an integer value from preferences
     *
     * @param context      the context
     * @param keyId        the key id
     * @param defaultValue the default value
     * @return stored int value
     */
    @SuppressWarnings("SameParameterValue")
    public static int getInt(Context context, String keyId, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(keyId, defaultValue);
    }

    /**
     * Sets an integer value to preferences
     *
     * @param context the context
     * @param keyId   the key id
     * @param value   the value to set
     */
    public static void setInt(Context context, String keyId, int value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putInt(keyId, value);
        editor.apply();
    }

    /**
     * Gets a long value from preferences
     *
     * @param context the context
     * @param keyId   the key id
     * @return the stored long value
     */
    @SuppressWarnings({"SameParameterValue", "unused"})
    public static long getLong(Context context, @StringRes int keyId) {
        return PreferencesUtil.getLong(context, getKey(context, keyId));
    }

    /**
     * Gets a long value from preferences
     *
     * @param context the context
     * @param keyId   the key id
     * @return the stored long value
     */
    public static long getLong(Context context, String code, @StringRes int keyId) {
        String key = String.format("%s_%s", code, getKey(context, keyId));
        return getLong(context, key);
    }

    /**
     * Gets a long value from preferences
     *
     * @param context the context
     * @param key     the key id
     * @return the stored long value
     */
    public static long getLong(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(key, -1L);
    }


    public static void setLong(Context context, String code, @StringRes int keyId, long value) {
        String key = String.format("%s_%s", code, getKey(context, keyId));
        setLong(context, key, value);
    }

    /**
     * Sets a long value to preferences
     *
     * @param context the context
     * @param keyId   the key id
     * @param value   the value
     */
    @SuppressWarnings({"SameParameterValue", "unused"})
    public static void setLong(Context context, @StringRes int keyId, long value) {
        setLong(context, getKey(context, keyId), value);
    }

    /**
     * Sets a long value to preferences
     *
     * @param context the context
     * @param key     the int key id
     */
    public static void setLong(Context context, String key, long value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * Gets a string value from preferences
     *
     * @param context      the context
     * @param keyId        the key id
     * @param defaultValue default value
     * @return the stored string value
     */
    @SuppressWarnings("SameParameterValue")
    public static String getString(Context context, @StringRes int keyId, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getString(getKey(context, keyId), defaultValue);
    }

    /**
     * Gets a string value from preferences
     *
     * @param context      the context
     * @param keyId        the key id
     * @param defaultValue default value
     * @return the stored string value
     */
    @SuppressWarnings("SameParameterValue")
    public static String getString(Context context, String keyId, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getString(keyId, defaultValue);
    }

    @SuppressWarnings("SameParameterValue")
    public static String getString(Context context, String code, @StringRes int keyId, String defaultValue) {
        String key = String.format("%s_%s", code, getKey(context, keyId));
        return getString(context, key, defaultValue);
    }

    /**
     * Sets a string value to preferences
     *
     * @param context the context
     * @param keyId   the key id
     */
    public static void setString(Context context, @StringRes int keyId, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putString(getKey(context, keyId), value);
        editor.apply();
    }

    @SuppressWarnings("SameParameterValue")
    public static void setString(Context context, String code, @StringRes int keyId, String value) {
        String key = String.format("%s_%s", code, getKey(context, keyId));
        setString(context, key, value);
    }

    /**
     * Sets a string value to preferences
     *
     * @param context the context
     * @param keyId   the key id
     */
    public static void setString(Context context, String keyId, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putString(keyId, value);
        editor.apply();
    }

    /**
     * Removes the key from the preferences
     *
     * @param context the context
     * @param key     the key id
     */
    public static void removeKey(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Checks if the key is already saved in the preferences
     *
     * @param context the context
     * @param keyId   the key id
     * @return true if found
     */
    @SuppressWarnings("SameParameterValue")
    public static boolean containsKey(Context context, @StringRes int keyId) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.contains(getKey(context, keyId));
    }
}