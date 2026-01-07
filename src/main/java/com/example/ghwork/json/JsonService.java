package com.example.ghwork.json;

import com.example.ghwork.api.JobResponse;
import com.example.ghwork.api.WorkflowResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;

/**
 * A wrapper around Google's Gson library, exposing only methods for serializing/deserializing domain data
 */
public class JsonService {

    private final Gson gson;

    public JsonService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    /**
     * Parses a "List workflow runs for a repository" route JSON response as a data object
     * @param json The JSON response as a string
     * @return A data object representing only the needed information from the JSON response
     */
    public WorkflowResponse parseWorkflowResponse(String json) {
        return this.gson.fromJson(json, WorkflowResponse.class);
    }

    /**
     * Parses a "List jobs for a workflow run" route JSON response as a data object
     * @param json The JSON response as a string
     * @return A data object representing only the needed information from the JSON response
     */
    public JobResponse parseJobResponse(String json) {
        return this.gson.fromJson(json, JobResponse.class);
    }
}
