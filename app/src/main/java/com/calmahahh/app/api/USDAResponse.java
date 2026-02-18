package com.calmahahh.app.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Maps the JSON response from USDA FoodData Central API (/foods/search).
 *
 * Response structure:
 * {
 *   "foods": [{
 *     "fdcId": "...",
 *     "description": "pizza, cheese",
 *     "foodNutrients": [
 *       { "nutrientId": 1008, "nutrient": { "name": "Energy", "unitName": "KCAL" }, "value": 280 },
 *       { "nutrientId": 1003, "nutrient": { "name": "Protein", "unitName": "G" }, "value": 12 }
 *     ]
 *   }]
 * }
 */
public class USDAResponse {

    public List<USDAFood> foods;

    /**
     * Returns the best (first) food match from the search results.
     */
    public USDAFood getBestMatch() {
        if (foods != null && !foods.isEmpty()) {
            return foods.get(0);
        }
        return null;
    }

    // --- Nested classes ---

    public static class USDAFood {
        public String fdcId;
        public String description;
        public List<FoodNutrient> foodNutrients;

        /**
         * Extracts per-100g calories from nutrient list.
         * Nutrient ID 1008 = Energy (KCAL).
         */
        public double getCalories() {
            if (foodNutrients == null) return 0;
            for (FoodNutrient fn : foodNutrients) {
                if (fn.nutrientId == 1008 && fn.value != null) {
                    return fn.value;
                }
            }
            return 0;
        }

        /**
         * Extracts per-100g protein from nutrient list.
         * Nutrient ID 1003 = Protein.
         */
        public double getProtein() {
            if (foodNutrients == null) return 0;
            for (FoodNutrient fn : foodNutrients) {
                if (fn.nutrientId == 1003 && fn.value != null) {
                    return fn.value;
                }
            }
            return 0;
        }
    }

    public static class FoodNutrient {
        public int nutrientId;
        public Nutrient nutrient;
        @SerializedName("value")
        public Double value;
    }

    public static class Nutrient {
        public String name;
        public String unitName;
    }
}
