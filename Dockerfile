# Build stage
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy Maven wrapper and pom first for dependency caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests -Dmaven.javadoc.skip=true

# Runtime stage - using a slim JRE image
FROM --platform=$TARGETPLATFORM eclipse-temurin:21-jre-jammy

# Create non-root user for security
RUN groupadd --system --gid 1000 springboot && \
    useradd --system --uid 1000 --gid springboot springboot

# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R springboot:springboot /app

USER springboot

# JVM tuning for containers
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseContainerSupport \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.backgroundpreinitializer.ignore=true"

# Expose application port
EXPOSE 9080

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
