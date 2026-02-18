package com.calmahahh.app.util;

import com.calmahahh.app.model.FoodItem;

import java.util.List;

/**
 * Utility class for nutrition calculations.
 * All macros scale linearly based on portion size:
 *   value = (valuePer100g * grams) / 100
 */
public class NutritionCalculator {

    public static double scale(double per100g, double grams) {
        return (per100g * grams) / 100.0;
    }

    public static double calculateCalories(double caloriesPer100g, double grams) {
        return scale(caloriesPer100g, grams);
    }

    public static double calculateProtein(double proteinPer100g, double grams) {
        return scale(proteinPer100g, grams);
    }

    public static double calculateTotalCalories(List<FoodItem> items) {
        double total = 0;
        for (FoodItem item : items) {
            total += item.getCalculatedCalories();
        }
        return total;
    }

    public static double calculateTotalProtein(List<FoodItem> items) {
        double total = 0;
        for (FoodItem item : items) {
            total += item.getCalculatedProtein();
        }
        return total;
    }

    public static double calculateTotalCarbs(List<FoodItem> items) {
        double total = 0;
        for (FoodItem item : items) {
            total += item.getCalculatedCarbs();
        }
        return total;
    }

    public static double calculateTotalFat(List<FoodItem> items) {
        double total = 0;
        for (FoodItem item : items) {
            total += item.getCalculatedFat();
        }
        return total;
    }
}
