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

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Utility class for managing Kafka schemas and topics.
 * This class provides methods to register schemas, create topics, and manage schema versions.
 */
public class KafkaSchemaManager {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSchemaManager.class);

    private final SchemaRegistryClient schemaRegistryClient;
    private final AdminClient adminClient;

    public KafkaSchemaManager(SchemaRegistryClient schemaRegistryClient, AdminClient adminClient) {
        this.schemaRegistryClient = schemaRegistryClient;
        this.adminClient = adminClient;
    }

    /**
     * Creates a Kafka topic if it doesn't already exist
     */
    public void createTopicIfNotExists(String topicName, int partitions, short replicationFactor)
            throws ExecutionException, InterruptedException {
        ListTopicsResult listTopics = adminClient.listTopics();
        Set<String> existingTopics = listTopics.names().get();

        if (!existingTopics.contains(topicName)) {
            logger.info("Creating topic: {}", topicName);
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
            result.all().get();
            logger.info("Topic {} created successfully", topicName);
        } else {
            logger.info("Topic {} already exists", topicName);
        }
    }

    /**
     * Registers a new schema version in the Schema Registry
     */
    public int registerSchema(String subject, String schemaString) throws IOException, RestClientException {
        AvroSchema schema = new AvroSchema(schemaString);
        int schemaId = schemaRegistryClient.register(subject, schema);
        logger.info("Registered schema for subject '{}' with ID: {}", subject, schemaId);
        return schemaId;
    }

    /**
     * Checks if a subject exists in the Schema Registry
     */
    public boolean subjectExists(String subject) {
        try {
            schemaRegistryClient.getAllVersions(subject);
            return true;
        } catch (IOException | RestClientException e) {
            return false;
        }
    }

    /**
     * Gets the latest schema version for a subject
     */
    public int getLatestSchemaVersion(String subject) throws IOException, RestClientException {
        SchemaMetadata metadata = schemaRegistryClient.getLatestSchemaMetadata(subject);
        return metadata.getVersion();
    }

    /**
     * Gets all versions of a schema for a subject
     */
    public java.util.List<Integer> getAllVersions(String subject) throws IOException, RestClientException {
        return schemaRegistryClient.getAllVersions(subject);
    }

    /**
     * Deletes a specific version of a schema (use with caution)
     */
    public void deleteSchemaVersion(String subject, int version) throws IOException, RestClientException {
        schemaRegistryClient.deleteSchemaVersion(subject, String.valueOf(version));
        logger.info("Deleted schema version {} for subject '{}'", version, subject);
    }
}