/*
 * Copyright 2023 Flamingock (https://www.flamingock.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.flamingock.examples.inventory.util;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Simple LaunchDarkly Management API client for creating and managing feature flags.
 * This demonstrates how teams would typically manage flags programmatically in real deployments.
 */
public class LaunchDarklyClient {
    private static final Logger logger = LoggerFactory.getLogger(LaunchDarklyClient.class);

    private final OkHttpClient httpClient;
    private final String apiToken;
    private final String projectKey;
    private final String environmentKey;
    private final String baseUrl;

    public LaunchDarklyClient(String apiToken, String projectKey, String environmentKey) {
        this(apiToken, projectKey, environmentKey, "http://localhost:8765/api/v2");
    }

    public LaunchDarklyClient(String apiToken, String projectKey, String environmentKey, String baseUrl) {
        this.httpClient = new OkHttpClient();
        this.apiToken = apiToken;
        this.projectKey = projectKey;
        this.environmentKey = environmentKey;
        this.baseUrl = baseUrl;

        logger.info("Initialized LaunchDarkly client for project: {}, environment: {}, baseUrl: {}",
                   projectKey, environmentKey, baseUrl);
    }

    /**
     * Creates a boolean feature flag
     */
    public void createBooleanFlag(String flagKey, String name, String description) throws IOException {
        String json = String.format("""
            {
              "key": "%s",
              "name": "%s",
              "description": "%s",
              "kind": "boolean",
              "variations": [
                {"value": true, "name": "True"},
                {"value": false, "name": "False"}
              ],
              "defaults": {
                "onVariation": 1,
                "offVariation": 0
              }
            }
            """, flagKey, name, description);

        Request request = new Request.Builder()
                .url(baseUrl + "/flags/" + projectKey)
                .header("Authorization", apiToken)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to create flag '" + flagKey + "': " + response.code() + " " + response.message());
            }
            logger.info("Created flag '{}' successfully", flagKey);
        }
    }

    /**
     * Creates a string feature flag with variations
     */
    public void createStringFlag(String flagKey, String name, String description, String[] variations) throws IOException {
        StringBuilder variationsJson = new StringBuilder();
        for (int i = 0; i < variations.length; i++) {
            if (i > 0) variationsJson.append(",");
            variationsJson.append(String.format("""
                {"value": "%s", "name": "%s"}
                """, variations[i], variations[i]));
        }

        String json = String.format("""
            {
              "key": "%s",
              "name": "%s",
              "description": "%s",
              "kind": "string",
              "variations": [%s],
              "defaults": {
                "onVariation": 0,
                "offVariation": 0
              }
            }
            """, flagKey, name, description, variationsJson.toString());

        Request request = new Request.Builder()
                .url(baseUrl + "/flags/" + projectKey)
                .header("Authorization", apiToken)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to create string flag '" + flagKey + "': " + response.code() + " " + response.message());
            }
            logger.info("Created string flag '{}' with {} variations", flagKey, variations.length);
        }
    }

    /**
     * Deletes a feature flag
     */
    public void deleteFlag(String flagKey) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/flags/" + projectKey + "/" + flagKey)
                .header("Authorization", apiToken)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to delete flag '" + flagKey + "': " + response.code() + " " + response.message());
            }
            logger.info("Deleted flag '{}' successfully", flagKey);
        }
    }

    /**
     * Archives a feature flag (soft delete)
     */
    public void archiveFlag(String flagKey) throws IOException {
        String json = """
            {
              "comment": "Archived by Flamingock - feature is now permanent"
            }
            """;

        Request request = new Request.Builder()
                .url(baseUrl + "/flags/" + projectKey + "/" + flagKey + "/archive")
                .header("Authorization", apiToken)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to archive flag '" + flagKey + "': " + response.code() + " " + response.message());
            }
            logger.info("Archived flag '{}' successfully", flagKey);
        }
    }

    /**
     * Checks if a flag exists
     */
    public boolean flagExists(String flagKey) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/flags/" + projectKey + "/" + flagKey)
                .header("Authorization", apiToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            boolean exists = response.isSuccessful();
            logger.debug("Flag '{}' exists: {}", flagKey, exists);
            return exists;
        }
    }
}