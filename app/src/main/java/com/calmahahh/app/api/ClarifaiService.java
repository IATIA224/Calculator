package com.calmahahh.app.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit interface for Clarifai's food-item-recognition model.
 * Uses the community model hosted at clarifai/main.
 */
public interface ClarifaiService {

    @POST("v2/users/clarifai/apps/main/models/{model_id}/outputs")
    Call<ClarifaiResponse> predict(
            @Path("model_id") String modelId,
            @Header("Authorization") String authorization,
            @Body ClarifaiRequest request
    );
}
