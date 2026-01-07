package com.example.ghwork.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Gson deserializer for an {@link java.time.Instant} object.
 */
public class InstantDeserializer implements JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return Instant.parse(json.getAsString());
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Failed to parse Instant: " + json.getAsString(), e);
        }
    }
}
