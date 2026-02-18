package com.calmahahh.app.model;

/**
 * Represents a detected food item with its nutritional data.
 * Stores per-100g values and the user-editable portion size in grams.
 * Includes all macros: calories, protein, carbs, fat.
 */
public class FoodItem {

    private String name;
    private double caloriesPer100g;
    private double proteinPer100g;
    private double carbsPer100g;
    private double fatPer100g;
    private double grams;          // user-editable portion
    private double confidence;

    public FoodItem(String name, double caloriesPer100g, double proteinPer100g,
                    double carbsPer100g, double fatPer100g,
                    double grams, double confidence) {
        this.name = name;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinPer100g = proteinPer100g;
        this.carbsPer100g = carbsPer100g;
        this.fatPer100g = fatPer100g;
        this.grams = grams;
        this.confidence = confidence;
    }

    // --- Getters ---

    public String getName()             { return name; }
    public double getCaloriesPer100g()  { return caloriesPer100g; }
    public double getProteinPer100g()   { return proteinPer100g; }
    public double getCarbsPer100g()     { return carbsPer100g; }
    public double getFatPer100g()       { return fatPer100g; }
    public double getGrams()            { return grams; }
    public double getConfidence()       { return confidence; }

    // --- Setters ---

    public void setName(String name) { this.name = name; }

    public void setGrams(double grams) {
        this.grams = Math.max(0, grams);
    }

    // --- Computed values (scale per-100g to actual portion) ---

    public double getCalculatedCalories() {
        return (caloriesPer100g * grams) / 100.0;
    }

    public double getCalculatedProtein() {
        return (proteinPer100g * grams) / 100.0;
    }

    public double getCalculatedCarbs() {
        return (carbsPer100g * grams) / 100.0;
    }

    public double getCalculatedFat() {
        return (fatPer100g * grams) / 100.0;
    }
}
