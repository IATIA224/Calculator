package com.calmahahh.app.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a single food entry within a meal.
 * Relationship: One date -> multiple meals -> multiple food entries.
 */
@Entity(tableName = "meal_entries")
public class MealEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String date; // yyyy-MM-dd

    @NonNull
    public String mealType; // "Breakfast", "Lunch", "Dinner"

    @NonNull
    public String foodName;

    public double calories;
    public double protein;
    public double carbs;
    public double fat;
    public double grams;

    public MealEntry(@NonNull String date, @NonNull String mealType,
                     @NonNull String foodName, double calories,
                     double protein, double carbs, double fat, double grams) {
        this.date = date;
        this.mealType = mealType;
        this.foodName = foodName;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.grams = grams;
    }
}
