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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for managing YAML configuration files.
 * Provides methods to read, write, and backup configuration files.
 */
public class ConfigFileManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFileManager.class);

    private final String configFilePath;
    private final Yaml yaml;

    public ConfigFileManager(String configFilePath) {
        this.configFilePath = configFilePath;

        // Configure YAML for pretty output
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        this.yaml = new Yaml(options);
    }

    /**
     * Reads the configuration from the YAML file
     */
    public Map<String, Object> readConfig() throws IOException {
        Path path = Paths.get(configFilePath);

        // Create default config if file doesn't exist
        if (!Files.exists(path)) {
            logger.info("Config file not found, creating default configuration at: {}", configFilePath);
            Map<String, Object> defaultConfig = createDefaultConfig();
            writeConfig(defaultConfig);
            return defaultConfig;
        }

        try (InputStream inputStream = new FileInputStream(configFilePath)) {
            Map<String, Object> config = yaml.load(inputStream);
            if (config == null) {
                config = new LinkedHashMap<>();
            }
            logger.debug("Loaded configuration from: {}", configFilePath);
            return config;
        }
    }

    /**
     * Writes the configuration to the YAML file
     */
    public void writeConfig(Map<String, Object> config) throws IOException {
        Path path = Paths.get(configFilePath);

        // Create directory if it doesn't exist
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            logger.info("Created config directory: {}", parentDir);
        }

        // Create backup before writing
        if (Files.exists(path)) {
            createBackup();
        }

        try (Writer writer = new FileWriter(configFilePath)) {
            yaml.dump(config, writer);
            logger.info("Configuration written to: {}", configFilePath);
        }
    }

    /**
     * Creates a backup of the current config file
     */
    private void createBackup() throws IOException {
        Path source = Paths.get(configFilePath);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = configFilePath + ".backup_" + timestamp;
        Path backup = Paths.get(backupFileName);

        Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
        logger.debug("Created backup: {}", backupFileName);
    }

    /**
     * Creates the default configuration structure
     */
    private Map<String, Object> createDefaultConfig() {
        Map<String, Object> config = new LinkedHashMap<>();

        // Application section
        Map<String, Object> application = new LinkedHashMap<>();
        application.put("name", "Inventory & Orders Service");
        application.put("version", "1.0.0");
        application.put("environment", "development");
        config.put("application", application);

        // Database section
        Map<String, Object> database = new LinkedHashMap<>();
        database.put("host", "localhost");
        database.put("port", 27017);
        database.put("name", "inventory");
        config.put("database", database);

        // Kafka section
        Map<String, Object> kafka = new LinkedHashMap<>();
        kafka.put("bootstrap.servers", "localhost:9092");
        kafka.put("schema.registry.url", "http://localhost:8081");
        config.put("kafka", kafka);

        // Features section (initially empty, will be populated by changes)
        config.put("features", new LinkedHashMap<>());

        // Metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("createdAt", LocalDateTime.now().toString());
        metadata.put("createdBy", "flamingock-init");
        config.put("metadata", metadata);

        return config;
    }

    /**
     * Gets a specific value from the config using dot notation
     * Example: getValue("features.discounts.enabled")
     */
    @SuppressWarnings("unchecked")
    public Object getValue(String path) throws IOException {
        Map<String, Object> config = readConfig();
        String[] parts = path.split("\\.");
        Object current = config;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    /**
     * Sets a specific value in the config using dot notation
     */
    @SuppressWarnings("unchecked")
    public void setValue(String path, Object value) throws IOException {
        Map<String, Object> config = readConfig();
        String[] parts = path.split("\\.");
        Map<String, Object> current = config;

        // Navigate to the parent map
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.containsKey(part)) {
                current.put(part, new LinkedHashMap<String, Object>());
            }
            current = (Map<String, Object>) current.get(part);
        }

        // Set the value
        current.put(parts[parts.length - 1], value);

        writeConfig(config);
    }
}