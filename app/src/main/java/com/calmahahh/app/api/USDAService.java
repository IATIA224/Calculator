package com.calmahahh.app.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Retrofit interface for USDA FoodData Central API.
 * Completely free — no credit card required, unlimited requests.
 *
 * API docs: https://fdc.nal.usda.gov/api-docs/
 */
public interface USDAService {

    /**
     * Search for foods by query string.
     *
     * @param query       food name (e.g., "pizza", "chicken")
     * @param pageSize    number of results (max 50)
     * @param pageNumber  page number (0-based)
     * @param sortBy      "dataType.keyword" to prioritize FoodData Central branded foods
     * @param apiKey      USDA API key from https://fdc.nal.usda.gov/api-key/
     */
    @GET("fds/v1/foods/search")
    Call<USDAResponse> searchFood(
            @Query("query") String query,
            @Query("pageSize") int pageSize,
            @Query("pageNumber") int pageNumber,
            @Query("sortBy") String sortBy,
            @Query("api_key") String apiKey
    );
}
