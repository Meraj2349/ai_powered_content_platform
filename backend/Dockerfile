# === Stage 1: Build ===
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only pom.xml and download dependencies first
COPY pom.xml .
RUN mvn -B dependency:resolve dependency:resolve-plugins

# Copy source code
COPY src ./src

# Build the app (skip tests for speed)
RUN mvn clean package -DskipTests

# === Stage 2: Run ===
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8090

# Run with active profile "prod"
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
