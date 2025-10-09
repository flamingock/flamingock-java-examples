package io.flamingock.examples.inventory.util;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public final class MongoDBUtil {

    private MongoDBUtil() {}

    public static MongoClient getMongoClient(String connectionString) {
        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        builder.applyConnectionString(new ConnectionString(connectionString));
        MongoClientSettings settings = builder.build();
        return MongoClients.create(settings);
    }
}
