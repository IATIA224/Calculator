package com.calmahahh.app.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for the Edamam Food Database API.
 * Free developer tier – provides nutrition data per 100 g for searched foods.
 */
public interface EdamamService {

    @GET("api/food-database/v2/parser")
    Call<EdamamResponse> searchFood(
            @Query("ingr") String ingredient,
            @Query("app_id") String appId,
            @Query("app_key") String appKey
    );
}
