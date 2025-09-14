package org.example.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * MongoDB Configuration for SkillMate AI Platform
 * Handles database connection, indexing, and optimization for educational data
 */
@Configuration
@EnableMongoRepositories(basePackages = "org.example.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

        @Value("${spring.data.mongodb.host:localhost}")
        private String host;

        @Value("${spring.data.mongodb.port:27017}")
        private int port;

        @Value("${spring.data.mongodb.database:ai_content_platform}")
        private String databaseName;

        @Value("${spring.data.mongodb.username:}")
        private String username;

        @Value("${spring.data.mongodb.password:}")
        private String password;

        @Value("${spring.data.mongodb.authentication-database:}")
        private String authDatabase;

        @Value("${spring.data.mongodb.auto-index-creation:true}")
        private boolean autoIndexCreation;

        /**
         * Database name for SkillMate AI Platform
         */
        @Override
        protected String getDatabaseName() {
                return databaseName;
        }

        /**
         * MongoDB Client configuration with connection pooling and optimization
         */
        @Override
        @Bean
        public MongoClient mongoClient() {
                ConnectionString connectionString;

                // Build connection string based on authentication
                if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                        if (authDatabase != null && !authDatabase.isEmpty()) {
                                connectionString = new ConnectionString(
                                                String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
                                                                username, password, host, port, databaseName,
                                                                authDatabase));
                        } else {
                                connectionString = new ConnectionString(
                                                String.format("mongodb://%s:%s@%s:%d/%s",
                                                                username, password, host, port, databaseName));
                        }
                } else {
                        connectionString = new ConnectionString(
                                        String.format("mongodb://%s:%d/%s", host, port, databaseName));
                }

                MongoClientSettings settings = MongoClientSettings.builder()
                                .applyConnectionString(connectionString)
                                // Connection pool settings for high-performance educational platform
                                .applyToConnectionPoolSettings(builder -> builder.maxSize(100) // Max connections for
                                                                                               // concurrent users
                                                .minSize(10) // Min connections for quick response
                                                .maxWaitTime(120000, java.util.concurrent.TimeUnit.MILLISECONDS) // 2
                                                                                                                 // minutes
                                                                                                                 // wait
                                                .maxConnectionLifeTime(30, java.util.concurrent.TimeUnit.MINUTES) // 30
                                                                                                                  // min
                                                                                                                  // lifetime
                                                .maxConnectionIdleTime(10, java.util.concurrent.TimeUnit.MINUTES) // 10
                                                                                                                  // min
                                                                                                                  // idle
                                )
                                // Socket settings for educational content delivery
                                .applyToSocketSettings(
                                                builder -> builder.connectTimeout(10000,
                                                                java.util.concurrent.TimeUnit.MILLISECONDS) // 10 sec
                                                                .readTimeout(60000,
                                                                                java.util.concurrent.TimeUnit.MILLISECONDS) // 60
                                                                                                                            // sec
                                                                                                                            // for
                                                                                                                            // large
                                                                                                                            // queries
                                )
                                // Server settings for reliability
                                .applyToServerSettings(
                                                builder -> builder.heartbeatFrequency(10000,
                                                                java.util.concurrent.TimeUnit.MILLISECONDS) // 10
                                                                                                            // sec
                                                                                                            // heartbeat
                                                                .minHeartbeatFrequency(500,
                                                                                java.util.concurrent.TimeUnit.MILLISECONDS) // 500ms
                                                                                                                            // min
                                )
                                .build();

                return MongoClients.create(settings);
        }

        /**
         * MongoDB Template with custom configurations for SkillMate AI
         */
        @Bean
        public MongoTemplate mongoTemplate() throws Exception {
                MongoTemplate template = new MongoTemplate(mongoClient(), getDatabaseName());

                // Configure converter to remove _class field (cleaner JSON storage)
                MappingMongoConverter converter = (MappingMongoConverter) template.getConverter();
                converter.setTypeMapper(new DefaultMongoTypeMapper(null));

                return template;
        }

        /**
         * Custom conversions for educational data types
         */
        @Override
        public MongoCustomConversions customConversions() {
                return new MongoCustomConversions(Collections.emptyList());
        }

        /**
         * Create specialized indexes for educational platform performance
         */
        private void createEducationalIndexes(MongoTemplate mongoTemplate) {
                // Index creation moved to MongoIndexInitializer to avoid circular dependencies
        }

        /**
         * Collection names for SkillMate AI Platform
         */
        @Override
        protected Collection<String> getMappingBasePackages() {
                return Collections.singleton("org.example.model");
        }

        /**
         * Configure field naming strategy for clean JSON storage
         */
        @Override
        protected boolean autoIndexCreation() {
                return autoIndexCreation;
        }
}
