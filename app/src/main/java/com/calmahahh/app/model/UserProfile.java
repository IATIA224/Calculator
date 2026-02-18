package com.calmahahh.app.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * User profile data model for TDEE calculation and calorie targets.
 * Persisted via SharedPreferences so the survey only runs once.
 */
public class UserProfile {

    private static final String PREFS_NAME = "calmahahh_user_profile";
    private static final String KEY_COMPLETED = "survey_completed";
    private static final String KEY_AGE = "age";
    private static final String KEY_WEIGHT = "weight_kg";
    private static final String KEY_HEIGHT = "height_cm";
    private static final String KEY_GENDER = "gender";           // "male" or "female"
    private static final String KEY_ACTIVITY = "activity_level";  // 1-5
    private static final String KEY_BODY_FAT = "body_fat";       // 0 = not provided
    private static final String KEY_GOAL = "goal";               // "cut", "maintain", "bulk"
    private static final String KEY_WEEKLY_RATE = "weekly_rate_kg";
    private static final String KEY_TARGET_CALORIES = "target_calories";

    private int age;
    private double weightKg;
    private double heightCm;
    private String gender;
    private int activityLevel;     // 1=sedentary, 2=lightly, 3=moderate, 4=very, 5=extra
    private double bodyFatPercent; // 0 means not provided
    private String goal;           // "cut", "maintain", "bulk"
    private double weeklyRateKg;   // how much to lose/gain per week in kg
    private int targetCalories;    // calculated daily target

    // Activity level multipliers (Harris-Benedict)
    public static final double[] ACTIVITY_MULTIPLIERS = {
        1.2,    // Sedentary (little/no exercise)
        1.375,  // Lightly active (1-3 days/week)
        1.55,   // Moderately active (3-5 days/week)
        1.725,  // Very active (6-7 days/week)
        1.9     // Extra active (very hard exercise, physical job)
    };

    public static final String[] ACTIVITY_LABELS = {
        "Sedentary (little/no exercise)",
        "Lightly active (1-3 days/week)",
        "Moderately active (3-5 days/week)",
        "Very active (6-7 days/week)",
        "Extra active (physical job + training)"
    };

    public static final String[] GOAL_LABELS = {
        "Cut (lose weight)",
        "Maintain weight",
        "Bulk (gain weight)"
    };

    public static final String[] GOAL_VALUES = {"cut", "maintain", "bulk"};

    public UserProfile() {}

    // ---- TDEE Calculation ----

    /**
     * Calculates BMR using Mifflin-St Jeor equation (default),
     * or Katch-McArdle if body fat percentage is provided.
     */
    public double calculateBMR() {
        if (bodyFatPercent > 0) {
            // Katch-McArdle formula (uses lean body mass)
            double leanMass = weightKg * (1.0 - bodyFatPercent / 100.0);
            return 370 + (21.6 * leanMass);
        } else {
            // Mifflin-St Jeor formula
            if ("male".equals(gender)) {
                return (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
            } else {
                return (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
            }
        }
    }

    /**
     * TDEE = BMR × activity multiplier
     */
    public double calculateTDEE() {
        double bmr = calculateBMR();
        int idx = Math.max(0, Math.min(activityLevel - 1, ACTIVITY_MULTIPLIERS.length - 1));
        return bmr * ACTIVITY_MULTIPLIERS[idx];
    }

    /**
     * Calculates daily calorie target based on TDEE and goal.
     * 1 kg of body weight ≈ 7700 kcal (mix of fat + muscle).
     * Daily surplus/deficit = (weeklyRateKg × 7700) / 7 = weeklyRateKg × 1100
     */
    public int calculateTargetCalories() {
        double tdee = calculateTDEE();
        double dailyAdjustment = weeklyRateKg * 1100; // kcal per day

        switch (goal != null ? goal : "maintain") {
            case "cut":
                targetCalories = (int) Math.round(tdee - dailyAdjustment);
                break;
            case "bulk":
                targetCalories = (int) Math.round(tdee + dailyAdjustment);
                break;
            default: // maintain
                targetCalories = (int) Math.round(tdee);
                break;
        }

        // Safety floor: never go below 1200 kcal
        targetCalories = Math.max(targetCalories, 1200);
        return targetCalories;
    }

    // ---- SharedPreferences persistence ----

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_COMPLETED, true)
                .putInt(KEY_AGE, age)
                .putFloat(KEY_WEIGHT, (float) weightKg)
                .putFloat(KEY_HEIGHT, (float) heightCm)
                .putString(KEY_GENDER, gender)
                .putInt(KEY_ACTIVITY, activityLevel)
                .putFloat(KEY_BODY_FAT, (float) bodyFatPercent)
                .putString(KEY_GOAL, goal)
                .putFloat(KEY_WEEKLY_RATE, (float) weeklyRateKg)
                .putInt(KEY_TARGET_CALORIES, targetCalories)
                .apply();
    }

    public static UserProfile load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        UserProfile p = new UserProfile();
        p.age = prefs.getInt(KEY_AGE, 25);
        p.weightKg = prefs.getFloat(KEY_WEIGHT, 70f);
        p.heightCm = prefs.getFloat(KEY_HEIGHT, 170f);
        p.gender = prefs.getString(KEY_GENDER, "male");
        p.activityLevel = prefs.getInt(KEY_ACTIVITY, 1);
        p.bodyFatPercent = prefs.getFloat(KEY_BODY_FAT, 0f);
        p.goal = prefs.getString(KEY_GOAL, "maintain");
        p.weeklyRateKg = prefs.getFloat(KEY_WEEKLY_RATE, 0.5f);
        p.targetCalories = prefs.getInt(KEY_TARGET_CALORIES, 2000);
        return p;
    }

    public static boolean isSurveyCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_COMPLETED, false);
    }

    public static void clearProfile(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    // ---- Getters / Setters ----

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getActivityLevel() { return activityLevel; }
    public void setActivityLevel(int activityLevel) { this.activityLevel = activityLevel; }

    public double getBodyFatPercent() { return bodyFatPercent; }
    public void setBodyFatPercent(double bodyFatPercent) { this.bodyFatPercent = bodyFatPercent; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public double getWeeklyRateKg() { return weeklyRateKg; }
    public void setWeeklyRateKg(double weeklyRateKg) { this.weeklyRateKg = weeklyRateKg; }

    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }
}
