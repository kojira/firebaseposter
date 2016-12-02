package net.kojira.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class Prefs {
    private static final String PREF_FILE_NAME_DEFAULT = "app_data";

    private static SharedPreferences getUserDataPreferences(Context context) {
        return context.getSharedPreferences(PREF_FILE_NAME_DEFAULT, Context.MODE_PRIVATE);
    }

    public static void remove(Context context, String key) {
        SharedPreferences sp = getUserDataPreferences(context);
        sp.edit().remove(key).apply();
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = getUserDataPreferences(context);
        sp.edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp = getUserDataPreferences(context);
        return sp.getString(key, "");
    }

    public static void putBytes(Context context, String key, byte[] value) {
        String strValue = Base64.encodeToString(value, 0);
        SharedPreferences sp = getUserDataPreferences(context);
        sp.edit().putString(key, strValue).apply();
    }

    public static byte[] getBytes(Context context, String key) {
        SharedPreferences sp = getUserDataPreferences(context);
        String strResult = sp.getString(key, "");
        return Base64.decode(strResult, 0);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = getUserDataPreferences(context);
        sp.edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sp = getUserDataPreferences(context);
        return sp.getInt(key, 0);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sp = getUserDataPreferences(context);
        return sp.getInt(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = getUserDataPreferences(context);
        sp.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sp = getUserDataPreferences(context);
        return sp.getBoolean(key, false);
    }

}
