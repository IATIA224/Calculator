package com.calmahahh.app;

/**
 * API key constants.
 *
 * ===================== SETUP INSTRUCTIONS =====================
 *
 * GOOGLE GEMINI (Vision AI — Food Recognition + Nutrition)
 * ─────────────────────────────────────────────────────────
 * - Go to https://aistudio.google.com/apikey
 * - Click "Create API Key"
 * - Free tier: 15 requests/minute, 1 million tokens/day
 * - No credit card required ✓
 * - Paste the API key below
 *
 * ==============================================================
 */
import com.calmahahh.app.BuildConfig;

public class Constants {

    // ======= GOOGLE GEMINI API =======
    // Free tier: 15 req/min, 1M tokens/day
    // Get key from: https://aistudio.google.com/apikey
    // API key is read from secrets.properties at build time
    public static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;

    // Meal type constants (Breakfast, Lunch, Dinner)
    public static final String MEAL_BREAKFAST = "Breakfast";
    public static final String MEAL_LUNCH = "Lunch";
    public static final String MEAL_DINNER = "Dinner";
    public static final String[] MEAL_TYPES = {MEAL_BREAKFAST, MEAL_LUNCH, MEAL_DINNER};
}
