package com.calmahahh.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Manages dark mode preferences and applies theme settings.
 */
public class DarkModeManager {
    private static final String PREF_NAME = "dark_mode_prefs";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    /**
     * Apply the saved dark mode preference on app startup.
     * Call this in MainActivity.onCreate() before setContentView().
     */
    public static void applyDarkModePreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int darkModeEnabled = prefs.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(darkModeEnabled);
    }

    /**
     * Check if dark mode is currently enabled.
     */
    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int mode = prefs.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        return mode == AppCompatDelegate.MODE_NIGHT_YES;
    }

    /**
     * Toggle dark mode and apply the change.
     */
    public static void toggleDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int currentMode = prefs.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        int newMode;
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            newMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newMode = AppCompatDelegate.MODE_NIGHT_YES;
        }

        // Save preference
        prefs.edit().putInt(KEY_DARK_MODE, newMode).apply();

        // Apply theme change
        AppCompatDelegate.setDefaultNightMode(newMode);
    }

    /**
     * Set dark mode to a specific mode (MODE_NIGHT_YES, MODE_NIGHT_NO, or MODE_NIGHT_FOLLOW_SYSTEM).
     */
    public static void setDarkMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_DARK_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
