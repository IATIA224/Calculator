package com.calmahahh.app.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Maps the JSON response from Edamam's Food Database API (/parser).
 *
 * The response contains:
 *  - "parsed" : exact matches
 *  - "hints"  : fuzzy / related matches
 *
 * Each food entry includes per-100 g nutrient data under "nutrients".
 */
public class EdamamResponse {

    public List<Parsed> parsed;
    public List<Hint> hints;

    /**
     * Returns the best food match – prefers parsed (exact) results,
     * falls back to hints.
     */
    public Food getBestMatch() {
        if (parsed != null && !parsed.isEmpty() && parsed.get(0).food != null) {
            return parsed.get(0).food;
        }
        if (hints != null && !hints.isEmpty() && hints.get(0).food != null) {
            return hints.get(0).food;
        }
        return null;
    }

    // --- Nested classes ---

    public static class Parsed {
        public Food food;
    }

    public static class Hint {
        public Food food;
    }

    public static class Food {
        public String foodId;
        public String label;
        public Nutrients nutrients;
    }

    /**
     * Nutrient values per 100 g as returned by Edamam.
     */
    public static class Nutrients {
        @SerializedName("ENERC_KCAL")
        public double calories;

        @SerializedName("PROCNT")
        public double protein;

        @SerializedName("FAT")
        public double fat;

        @SerializedName("CHOCDF")
        public double carbs;

        @SerializedName("FIBTG")
        public double fiber;
    }
}
