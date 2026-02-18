package com.calmahahh.app.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit interface for Google Gemini API.
 * Uses Gemini 2.5 Flash (latest, most capable).
 *
 * Free tier: 15 requests/minute, 1 million tokens/day.
 * No credit card required.
 *
 * Endpoint: POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
 */
public interface GeminiService {

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    Call<GeminiResponse> generateContent(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );
}
