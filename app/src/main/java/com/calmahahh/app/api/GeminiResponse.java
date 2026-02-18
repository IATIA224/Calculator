package com.calmahahh.app.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Maps the JSON response from Gemini API.
 *
 * Response structure:
 * {
 *   "candidates": [{
 *     "content": {
 *       "parts": [{ "text": "{...json string from AI...}" }]
 *     }
 *   }]
 * }
 */
public class GeminiResponse {

    public List<Candidate> candidates;

    /**
     * Returns the text content from the first candidate's first part.
     * This is the JSON string the AI generated.
     */
    public String getText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate c = candidates.get(0);
            if (c.content != null && c.content.parts != null && !c.content.parts.isEmpty()) {
                return c.content.parts.get(0).text;
            }
        }
        return null;
    }

    // --- Nested classes ---

    public static class Candidate {
        public Content content;

        @SerializedName("finishReason")
        public String finishReason;
    }

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;
    }
}
