package com.ghwork.json;

import com.ghwork.api.JobResponse;
import com.ghwork.api.WorkflowResponse;
import com.ghwork.api.exceptions.InvalidJsonResponseException;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

/**
 * A wrapper around Google's Gson library, exposing only methods for serializing/deserializing
 * domain data.
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

    private <T> T parse(String json, Class<T> type) throws InvalidJsonResponseException {
        T result;
        try {
            result = this.gson.fromJson(json, type);
        } catch (RuntimeException e) {
            throw new InvalidJsonResponseException(type.getSimpleName(), e.getMessage(), e);
        }
        if (result == null) {
            throw new InvalidJsonResponseException(type.getSimpleName(), "the response is empty",
                null);
        }
        return result;
    }

    /**
     * Parses a "List workflow runs for a repository" route JSON response as a data object.
     *
     * @param json The JSON response as a string
     * @return A data object representing only the necessary information from the JSON response
     * @throws InvalidJsonResponseException If the response cannot be parsed
     */
    public WorkflowResponse parseWorkflowResponse(String json) throws InvalidJsonResponseException {
        return parse(json, WorkflowResponse.class);
    }

    /**
     * Parses a "List jobs for a workflow run" route JSON response as a data object.
     *
     * @param json The JSON response as a string
     * @return A data object representing only the necessary information from the JSON response
     * @throws InvalidJsonResponseException If the response cannot be parsed
     */
    public JobResponse parseJobResponse(String json) throws InvalidJsonResponseException {
        return parse(json, JobResponse.class);
    }
}
