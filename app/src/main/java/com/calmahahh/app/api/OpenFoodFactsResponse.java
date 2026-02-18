package com.calmahahh.app.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Maps the JSON response from OpenFoodFacts API (/api/v0/products).
 *
 * Response structure:
 * {
 *   "products": [{
 *     "product_name": "pizza",
 *     "generic_name": "cheese pizza",
 *     "nutriments": {
 *       "energy-kcal_100g": 280,
 *       "proteins_100g": 12,
 *       "fat_100g": 10
 *     }
 *   }]
 * }
 */
public class OpenFoodFactsResponse {

    public List<Product> products;

    /**
     * Returns the best (first) product match.
     */
    public Product getBestMatch() {
        if (products != null && !products.isEmpty()) {
            return products.get(0);
        }
        return null;
    }

    // --- Nested classes ---

    public static class Product {
        @SerializedName("product_name")
        public String productName;

        @SerializedName("generic_name")
        public String genericName;

        public Nutriments nutriments;

        public String getName() {
            return productName != null ? productName : genericName;
        }

        public double getCalories() {
            if (nutriments != null && nutriments.energyKcal100g != null) {
                return nutriments.energyKcal100g;
            }
            return 0;
        }

        public double getProtein() {
            if (nutriments != null && nutriments.proteins100g != null) {
                return nutriments.proteins100g;
            }
            return 0;
        }
    }

    public static class Nutriments {
        @SerializedName("energy-kcal_100g")
        public Double energyKcal100g;

        @SerializedName("proteins_100g")
        public Double proteins100g;

        @SerializedName("fat_100g")
        public Double fat100g;

        @SerializedName("carbohydrates_100g")
        public Double carbs100g;
    }
}
