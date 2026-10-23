package com.ghwork.json;

import com.ghwork.api.JobResponse;
import com.ghwork.api.WorkflowResponse;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

/**
 * A wrapper around Google's Gson library, exposing only methods for
 * serializing/deserializing domain data.
 */
@Service
public class JsonService {

    private final Gson gson;

    /**
     * Constructor for the service handling JSON (de)serialization.
     */
    public JsonService(Gson gson) {
        this.gson = gson;
    }

    /**
     * Parses a "List workflow runs for a repository" route JSON response as a data object.
     *
     * @param json The JSON response as a string
     * @return A data object representing only the necessary information from the JSON response
     */
    public WorkflowResponse parseWorkflowResponse(String json) {
        return this.gson.fromJson(json, WorkflowResponse.class);
    }

    /**
     * Parses a "List jobs for a workflow run" route JSON response as a data object.
     *
     * @param json The JSON response as a string
     * @return A data object representing only the necessary information from the JSON response
     */
    public JobResponse parseJobResponse(String json) {
        return this.gson.fromJson(json, JobResponse.class);
    }
}
