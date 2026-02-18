package com.calmahahh.app.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for OpenFoodFacts API.
 * Completely free — no API key required, unlimited requests.
 *
 * API docs: https://wiki.openfoodfacts.org/API
 */
public interface OpenFoodFactsService {

    /**
     * Search for foods by query string.
     *
     * @param search  food name (e.g., "pizza", "apple")
     * @param pageSize number of results (max 20)
     */
    @GET("api/v0/products")
    Call<OpenFoodFactsResponse> searchFood(
            @Query("search_terms") String search,
            @Query("page_size") int pageSize
    );
}
