package com.suryashakti.solar.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.suryashakti.solar.R;

public class ThemeManager {

    private static final String PREFS_NAME = "surya_prefs";
    private static final String KEY_THEME = "theme";
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";

    public static void saveTheme(Context context, String theme) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public static String getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_THEME, THEME_DARK); // default = dark
    }

    public static boolean isDark(Context context) {
        return THEME_DARK.equals(getSavedTheme(context));
    }

    public static int getThemeResId(Context context) {
        return isDark(context)
                ? R.style.Theme_SuryaShakti_Dark
                : R.style.Theme_SuryaShakti_Light;
    }

    public static void applyTheme(android.app.Activity activity) {
        activity.setTheme(getThemeResId(activity));
    }
}
