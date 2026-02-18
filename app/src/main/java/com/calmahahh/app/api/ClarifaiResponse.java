package com.calmahahh.app.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps the JSON response from Clarifai's Predict API.
 *
 * Structure:
 * {
 *   "status": { "code": 10000, "description": "Ok" },
 *   "outputs": [{
 *     "data": {
 *       "concepts": [
 *         { "id": "...", "name": "pizza", "value": 0.95 },
 *         ...
 *       ]
 *     }
 *   }]
 * }
 */
public class ClarifaiResponse {

    public Status status;
    public List<Output> outputs;

    /**
     * Returns the top concepts whose confidence is at least {@code minConfidence},
     * limited to {@code maxCount} results.
     */
    public List<Concept> getTopConcepts(double minConfidence, int maxCount) {
        List<Concept> result = new ArrayList<>();
        if (outputs == null || outputs.isEmpty()) return result;

        Output output = outputs.get(0);
        if (output.data == null || output.data.concepts == null) return result;

        for (Concept c : output.data.concepts) {
            if (c.value >= minConfidence && result.size() < maxCount) {
                result.add(c);
            }
        }
        return result;
    }

    // --- Nested classes ---

    public static class Status {
        public int code;
        public String description;
    }

    public static class Output {
        public OutputData data;
    }

    public static class OutputData {
        public List<Concept> concepts;
    }

    public static class Concept {
        public String id;
        public String name;
        public double value; // confidence 0.0–1.0
    }
}
