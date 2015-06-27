package com.insa.burnd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

// SharedPreferences class used to deal with local memory,
// makes (saving, loading) calls very easy -> SPManager.save/load()
public class SPManager {

    private SPManager() {
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static String load(Context ctx, String flag) {
        return getSharedPreferences(ctx)
                .getString(flag, "");
    }

    public static Boolean loadBoolean(Context ctx, String flag) {
        return getSharedPreferences(ctx)
                .getBoolean(flag, false);
    }
    public static void save(Context ctx, String value, String flag) {
        final SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(flag, value)
                .apply();
    }

    public static void remove(Context ctx, String flag) {
        final SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(flag)
                .apply();
    }

    public static void clear(Context ctx) {
        final SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear()
                .apply();
    }
}