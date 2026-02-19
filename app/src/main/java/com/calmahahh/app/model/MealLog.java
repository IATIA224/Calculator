package com.calmahahh.app.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Tracks daily meal logs: Breakfast, Lunch, Dinner.
 * Each meal stores a list of FoodItems and their total calories.
 * Persisted daily via SharedPreferences (keyed by date).
 */
public class MealLog {

    public static final int MEAL_BREAKFAST = 0;
    public static final int MEAL_LUNCH     = 1;
    public static final int MEAL_DINNER    = 2;

    public static final String[] MEAL_NAMES = {"Breakfast", "Lunch", "Dinner"};

    private static final String PREFS_NAME = "calmahahh_meal_log";
    private static final Gson gson = new Gson();

    /** A single logged meal entry */
    public static class MealEntry {
        public String mealName;    // "Breakfast", "Lunch", "Dinner"
        public List<FoodSnapshot> foods;
        public double totalCalories;
        public double totalProtein;
        public double totalCarbs;
        public double totalFat;

        public MealEntry(String mealName) {
            this.mealName = mealName;
            this.foods = new ArrayList<>();
        }
    }

    /** Snapshot of a food item at the time it was logged */
    public static class FoodSnapshot {
        public String name;
        public double grams;
        public double calories;
        public double protein;
        public double carbs;
        public double fat;

        public FoodSnapshot(String name, double grams, double calories,
                            double protein, double carbs, double fat) {
            this.name = name;
            this.grams = grams;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
        }
    }

    /** Daily log: up to 3 meals */
    public static class DailyLog {
        public String date; // yyyy-MM-dd
        public List<MealEntry> meals;

        public DailyLog(String date) {
            this.date = date;
            this.meals = new ArrayList<>();
        }

        public double getTotalCalories() {
            double total = 0;
            for (MealEntry m : meals) total += m.totalCalories;
            return total;
        }

        public double getTotalProtein() {
            double total = 0;
            for (MealEntry m : meals) total += m.totalProtein;
            return total;
        }

        public double getTotalCarbs() {
            double total = 0;
            for (MealEntry m : meals) total += m.totalCarbs;
            return total;
        }

        public double getTotalFat() {
            double total = 0;
            for (MealEntry m : meals) total += m.totalFat;
            return total;
        }

        /** Get total calories consumed for a specific meal type */
        public double getMealCalories(String mealName) {
            for (MealEntry m : meals) {
                if (m.mealName.equals(mealName)) return m.totalCalories;
            }
            return 0;
        }
    }

    // ---- Static helpers ----

    private static String todayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    /** Load today's log (or empty if none exists) */
    public static DailyLog loadToday(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = todayKey();
        String json = prefs.getString(key, null);
        if (json != null) {
            try {
                return gson.fromJson(json, DailyLog.class);
            } catch (Exception ignored) {}
        }
        return new DailyLog(key);
    }

    /** Save today's log */
    public static void saveToday(Context context, DailyLog log) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(todayKey(), gson.toJson(log)).apply();
    }

    /**
     * Add a meal to today's log from scanned food items.
     * @param context  Android context
     * @param mealType MEAL_BREAKFAST, MEAL_LUNCH, or MEAL_DINNER
     * @param items    List of food items from the AI scan
     */
    public static void addMeal(Context context, int mealType, List<FoodItem> items) {
        addMealByName(context, MEAL_NAMES[mealType], items);
    }

    /**
     * Add a meal to today's log by meal name string.
     * @param context  Android context
     * @param mealName "Breakfast", "Lunch", "Dinner", "Morning", "Afternoon", or "Evening"
     * @param items    List of food items from the AI scan
     */
    public static void addMealByName(Context context, String mealName, List<FoodItem> items) {
        DailyLog log = loadToday(context);

        // Remove existing entry for this meal (replace it)
        log.meals.removeIf(m -> m.mealName.equals(mealName));

        MealEntry entry = new MealEntry(mealName);
        double totalCal = 0, totalPro = 0, totalCarb = 0, totalFat = 0;

        for (FoodItem item : items) {
            double cal = item.getCalculatedCalories();
            double pro = item.getCalculatedProtein();
            double carb = item.getCalculatedCarbs();
            double fat = item.getCalculatedFat();

            entry.foods.add(new FoodSnapshot(
                    item.getName(), item.getGrams(), cal, pro, carb, fat));

            totalCal += cal;
            totalPro += pro;
            totalCarb += carb;
            totalFat += fat;
        }

        entry.totalCalories = totalCal;
        entry.totalProtein = totalPro;
        entry.totalCarbs = totalCarb;
        entry.totalFat = totalFat;

        log.meals.add(entry);
        saveToday(context, log);
    }
}
