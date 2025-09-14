package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

/**
 * MongoDB Index Initializer for SkillMate AI Platform
 * Handles index creation after application startup to avoid circular
 * dependencies
 */
@Component
public class MongoIndexInitializer {

        @Autowired
        private MongoTemplate mongoTemplate;

        @Value("${spring.data.mongodb.auto-index-creation:true}")
        private boolean autoIndexCreation;

        /**
         * Initialize database indexes after application is ready
         * Educational platforms require fast search and analytics
         */
        @EventListener(ApplicationReadyEvent.class)
        public void initializeIndexes() {
                if (!autoIndexCreation) {
                        return;
                }

                try {
                        // Create indexes for educational data optimization
                        createEducationalIndexes();

                        System.out.println("✅ MongoDB indexes created successfully for SkillMate AI Platform");
                } catch (Exception e) {
                        System.err.println("⚠️ Failed to create MongoDB indexes: " + e.getMessage());
                }
        }

        /**
         * Create specialized indexes for educational platform performance
         */
        private void createEducationalIndexes() {

                // User collection indexes
                IndexOperations userIndexOps = mongoTemplate.indexOps("users");

                try {
                        // Check existing indexes to avoid conflicts
                        var existingIndexes = userIndexOps.getIndexInfo().stream()
                                        .map(indexInfo -> indexInfo.getName())
                                        .toList();

                        if (!existingIndexes.contains("username_1")) {
                                userIndexOps.ensureIndex(new Index()
                                                .on("username", Sort.Direction.ASC)
                                                .unique()
                                                .named("username_1"));
                        }

                        if (!existingIndexes.contains("email_1")) {
                                userIndexOps.ensureIndex(new Index()
                                                .on("email", Sort.Direction.ASC)
                                                .unique()
                                                .named("email_1"));
                        }

                        // Create sparse index for firebaseUid (since it's optional)
                        if (!existingIndexes.contains("firebaseUid_1_sparse")) {
                                userIndexOps.ensureIndex(new Index()
                                                .on("firebaseUid", Sort.Direction.ASC)
                                                .sparse()
                                                .named("firebaseUid_1_sparse"));
                        }

                        System.out.println("✅ User indexes checked/created successfully");
                } catch (Exception e) {
                        System.err.println("⚠️ User indexes already exist or conflict detected, skipping: "
                                        + e.getMessage());
                }

                // Course collection indexes for fast search
                IndexOperations courseIndexOps = mongoTemplate.indexOps("courses");
                try {
                        // Check existing indexes to avoid conflicts
                        var existingIndexes = courseIndexOps.getIndexInfo().stream()
                                        .map(indexInfo -> indexInfo.getName())
                                        .toList();

                        if (!existingIndexes.contains("title_1")) {
                                courseIndexOps.ensureIndex(new Index()
                                                .on("title", Sort.Direction.ASC)
                                                .named("title_1"));
                        }

                        if (!existingIndexes.contains("platform_1")) {
                                courseIndexOps.ensureIndex(new Index()
                                                .on("platform", Sort.Direction.ASC)
                                                .named("platform_1"));
                        }

                        if (!existingIndexes.contains("technologies_1")) {
                                courseIndexOps.ensureIndex(new Index()
                                                .on("technologies", Sort.Direction.ASC)
                                                .named("technologies_1"));
                        }

                        if (!existingIndexes.contains("qualityScore_-1")) {
                                courseIndexOps.ensureIndex(new Index()
                                                .on("qualityScore", Sort.Direction.DESC)
                                                .named("qualityScore_-1"));
                        }

                        // Compound index for course search optimization
                        if (!existingIndexes.contains("platform_1_qualityScore_-1")) {
                                courseIndexOps.ensureIndex(new Index()
                                                .on("platform", Sort.Direction.ASC)
                                                .on("qualityScore", Sort.Direction.DESC)
                                                .named("platform_1_qualityScore_-1"));
                        }

                        System.out.println("✅ Course indexes checked/created successfully");
                } catch (Exception e) {
                        System.err.println("⚠️ Course indexes already exist or conflict detected, skipping: "
                                        + e.getMessage());
                }

                // Content collection indexes for AI-generated content
                IndexOperations contentIndexOps = mongoTemplate.indexOps("content");
                try {
                        contentIndexOps.ensureIndex(new Index()
                                        .on("author.id", Sort.Direction.ASC));
                        contentIndexOps.ensureIndex(new Index()
                                        .on("contentType", Sort.Direction.ASC));
                        contentIndexOps.ensureIndex(new Index()
                                        .on("status", Sort.Direction.ASC));
                        contentIndexOps.ensureIndex(new Index()
                                        .on("createdAt", Sort.Direction.DESC));
                } catch (Exception e) {
                        System.err.println("⚠️ Content indexes already exist or conflict detected, skipping: "
                                        + e.getMessage());
                }

                // Text search indexes for educational content discovery
                try {
                        // Check if a text index already exists on courses collection
                        boolean courseTextIndexExists = courseIndexOps.getIndexInfo().stream()
                                        .anyMatch(indexInfo -> indexInfo.getName().contains("text"));

                        if (!courseTextIndexExists) {
                                TextIndexDefinition courseTextIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                                                .onField("title")
                                                .onField("description")
                                                .onField("technologies")
                                                .build();
                                courseIndexOps.ensureIndex(courseTextIndex);
                                System.out.println("✅ Created text index for courses collection");
                        } else {
                                System.out.println("ℹ️ Text index already exists for courses collection, skipping");
                        }

                        // Check if a text index already exists on content collection
                        boolean contentTextIndexExists = contentIndexOps.getIndexInfo().stream()
                                        .anyMatch(indexInfo -> indexInfo.getName().contains("text"));

                        if (!contentTextIndexExists) {
                                TextIndexDefinition contentTextIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                                                .onField("title")
                                                .onField("description")
                                                .onField("content")
                                                .build();
                                contentIndexOps.ensureIndex(contentTextIndex);
                                System.out.println("✅ Created text index for content collection");
                        } else {
                                System.out.println("ℹ️ Text index already exists for content collection, skipping");
                        }
                } catch (Exception e) {
                        System.err.println("⚠️ Text indexes already exist or conflict detected, skipping: "
                                        + e.getMessage());
                }

                // Learning Path collection indexes
                IndexOperations learningPathIndexOps = mongoTemplate.indexOps("learning_paths");
                try {
                        learningPathIndexOps.ensureIndex(new Index()
                                        .on("name", Sort.Direction.ASC));
                        learningPathIndexOps.ensureIndex(new Index()
                                        .on("difficultyLevel", Sort.Direction.ASC));
                        learningPathIndexOps.ensureIndex(new Index()
                                        .on("estimatedHours", Sort.Direction.ASC));
                        learningPathIndexOps.ensureIndex(new Index()
                                        .on("createdAt", Sort.Direction.DESC));
                } catch (Exception e) {
                        System.err.println("⚠️ Learning Path indexes already exist or conflict detected, skipping: "
                                        + e.getMessage());
                }

                // GitHub Repository indexes for code analysis
                IndexOperations repoIndexOps = mongoTemplate.indexOps("github_repositories");
                try {
                        repoIndexOps.ensureIndex(new Index()
                                        .on("fullName", Sort.Direction.ASC)
                                        .unique());
                        repoIndexOps.ensureIndex(new Index()
                                        .on("language", Sort.Direction.ASC));
                        repoIndexOps.ensureIndex(new Index()
                                        .on("stargazersCount", Sort.Direction.DESC));
                        repoIndexOps.ensureIndex(new Index()
                                        .on("lastAnalyzed", Sort.Direction.DESC));
                } catch (Exception e) {
                        System.err.println("⚠️ GitHub Repository indexes already exist or conflict detected, skipping: "
                                        + e.getMessage());
                }

                // Analytics and Metrics indexes for platform insights
                IndexOperations metricsIndexOps = mongoTemplate.indexOps("user_metrics");
                try {
                        metricsIndexOps.ensureIndex(new Index()
                                        .on("userId", Sort.Direction.ASC));
                        metricsIndexOps.ensureIndex(new Index()
                                        .on("date", Sort.Direction.DESC));
                        metricsIndexOps.ensureIndex(new Index()
                                        .on("userId", Sort.Direction.ASC)
                                        .on("date", Sort.Direction.DESC));
                } catch (Exception e) {
                        System.err.println("⚠️ User Metrics indexes already exist or conflict detected, skipping: "
                                        + e.getMessage());
                }

                // API Usage tracking for rate limiting and analytics
                IndexOperations apiUsageIndexOps = mongoTemplate.indexOps("api_usage");
                try {
                        apiUsageIndexOps.ensureIndex(new Index()
                                        .on("apiKey", Sort.Direction.ASC));
                        apiUsageIndexOps.ensureIndex(new Index()
                                        .on("timestamp", Sort.Direction.DESC));
                        apiUsageIndexOps.ensureIndex(new Index()
                                        .on("endpoint", Sort.Direction.ASC));
                } catch (Exception e) {
                        System.err.println("⚠️ API Usage indexes already exist or conflict detected, skipping: "
                                        + e.getMessage());
                }
                apiUsageIndexOps.ensureIndex(new Index()
                                .on("timestamp", Sort.Direction.DESC));
                apiUsageIndexOps.ensureIndex(new Index()
                                .on("endpoint", Sort.Direction.ASC));
        }
}
