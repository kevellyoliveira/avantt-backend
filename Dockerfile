# Multi-stage Dockerfile for building and running the Spring Boot backend
# Build stage: use Maven with Java 17 to compile and produce the executable jar
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy only the pom first to leverage Docker layer caching for dependencies
COPY pom.xml ./

# Download dependencies (speeds up rebuilds)
RUN mvn -B -f pom.xml dependency:go-offline

# Copy source and build the project. We skip tests during image build because
# tests are executed separately as part of the verification step.
COPY src ./src
RUN mvn -B -f pom.xml package -DskipTests

# Runtime stage: use a lightweight OpenJDK 17 image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the jar produced by the builder stage
COPY --from=builder /workspace/target/*.jar ./app.jar

# Expose default Spring Boot port (can be overridden via SPRING_PORT or server.port)
EXPOSE 8080

# Use java -jar to run the Spring Boot fat jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
