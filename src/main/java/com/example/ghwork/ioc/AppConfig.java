package com.example.ghwork.ioc;

import com.example.ghwork.json.InstantDeserializer;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.http.HttpClient;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring IoC container configuration class for the application.
 */
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {
    /**
     * Method for producing the {@link GsonBuilder} bean.
     *
     * @return The {@link GsonBuilder} bean.
     */
    @Bean
    public GsonBuilder gsonBuilder() {
        return new GsonBuilder();
    }

    /**
     * Method for producing the {@link Gson} bean.
     *
     * @param gsonBuilder         The builder for the {@link Gson} object.
     * @param instantDeserializer The deserializer for {@link Instant} objects.
     * @return The {@link Gson} bean.
     */
    @Bean
    public Gson gson(GsonBuilder gsonBuilder, InstantDeserializer instantDeserializer) {
        return gsonBuilder
            .registerTypeAdapter(Instant.class, instantDeserializer)
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    }

    /**
     * Method for producing the {@link HttpClient} bean.
     *
     * @return The {@link HttpClient} bean.
     */
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
}
