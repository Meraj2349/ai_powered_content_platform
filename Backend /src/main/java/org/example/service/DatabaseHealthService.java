package org.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseHealthService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public boolean isDatabaseHealthy() {
        try {
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            // Basic database info
            String dbName = mongoTemplate.getDb().getName();
            info.put("database", dbName);
            info.put("status", "connected");

            // Collection information
            List<String> collections = new ArrayList<>();
            mongoTemplate.getDb().listCollectionNames().into(collections);
            info.put("collections", collections);
            info.put("collectionCount", collections.size());

            // Collection statistics
            Map<String, Long> collectionStats = new HashMap<>();
            for (String collection : collections) {
                try {
                    long count = mongoTemplate.getCollection(collection).countDocuments();
                    collectionStats.put(collection, count);
                } catch (Exception e) {
                    collectionStats.put(collection, -1L); // Error counting
                }
            }
            info.put("documentCounts", collectionStats);

            // Server status
            Document serverStatus = mongoTemplate.getDb().runCommand(new Document("serverStatus", 1));
            info.put("mongoVersion", serverStatus.get("version"));
            info.put("uptime", serverStatus.get("uptime"));

        } catch (Exception e) {
            info.put("status", "error");
            info.put("error", e.getMessage());
        }

        return info;
    }

    public boolean testConnection() {
        try {
            // Try to perform a simple operation
            mongoTemplate.getDb().runCommand(new Document("ismaster", 1));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getDetailedStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Connection test
            status.put("connectionTest", testConnection());

            // Database operations test
            status.put("pingTest", isDatabaseHealthy());

            // Get database info
            status.putAll(getDatabaseInfo());

            status.put("overall", "healthy");

        } catch (Exception e) {
            status.put("overall", "unhealthy");
            status.put("error", e.getMessage());
        }

        return status;
    }
}
