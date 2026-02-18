package com.calmahahh.app.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body for the Clarifai Predict API.
 * Wraps a Base64-encoded image inside the required JSON structure.
 */
public class ClarifaiRequest {

    public List<Input> inputs;

    /**
     * Factory method – creates a request payload containing one Base64 image.
     */
    public static ClarifaiRequest create(String base64Image) {
        ClarifaiRequest request = new ClarifaiRequest();
        request.inputs = new ArrayList<>();

        ImageData image = new ImageData();
        image.base64 = base64Image;

        InputData data = new InputData();
        data.image = image;

        Input input = new Input();
        input.data = data;

        request.inputs.add(input);
        return request;
    }

    // --- Nested classes matching the Clarifai JSON schema ---

    public static class Input {
        public InputData data;
    }

    public static class InputData {
        public ImageData image;
    }

    public static class ImageData {
        public String base64;
    }
}
