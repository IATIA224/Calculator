package com.calmahahh.app.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds the JSON request body for the Gemini API.
 *
 * Structure:
 * {
 *   "contents": [{ "parts": [
 *     {"text": "prompt"},
 *     {"inline_data": {"mime_type": "image/jpeg", "data": "base64..."}}
 *   ]}],
 *   "generationConfig": { "temperature": 0.1, "responseMimeType": "application/json" }
 * }
 */
public class GeminiRequest {

    public List<Content> contents;
    public GenerationConfig generationConfig;

    /**
     * Creates a request with image + text prompt.
     *
     * @param prompt    Structured text prompt
     * @param base64Img Base64-encoded JPEG image
     */
    public static GeminiRequest create(String prompt, String base64Img) {
        GeminiRequest req = new GeminiRequest();

        // Parts: text + image
        Part textPart = new Part();
        textPart.text = prompt;

        Part imagePart = new Part();
        imagePart.inlineData = new InlineData();
        imagePart.inlineData.mimeType = "image/jpeg";
        imagePart.inlineData.data = base64Img;

        List<Part> parts = new ArrayList<>();
        parts.add(textPart);
        parts.add(imagePart);

        Content content = new Content();
        content.parts = parts;

        req.contents = Collections.singletonList(content);

        // Generation config: low temperature for accuracy, JSON output
        req.generationConfig = new GenerationConfig();
        req.generationConfig.temperature = 0.2;
        req.generationConfig.responseMimeType = "application/json";

        return req;
    }

    // --- Nested classes ---

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;

        @SerializedName("inline_data")
        public InlineData inlineData;
    }

    public static class InlineData {
        @SerializedName("mime_type")
        public String mimeType;

        public String data;
    }

    public static class GenerationConfig {
        public double temperature;

        @SerializedName("responseMimeType")
        public String responseMimeType;
    }
}
