package com.calmahahh.app.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides singleton Retrofit instance for Google Gemini API.
 */
public class ApiClient {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/";

    private static Retrofit geminiRetrofit;

    private static OkHttpClient createClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();
    }

    public static synchronized GeminiService getGeminiService() {
        if (geminiRetrofit == null) {
            geminiRetrofit = new Retrofit.Builder()
                    .baseUrl(GEMINI_BASE_URL)
                    .client(createClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return geminiRetrofit.create(GeminiService.class);
    }
}
