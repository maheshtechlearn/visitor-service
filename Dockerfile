# Use a minimal base image with Java 17 JRE
FROM eclipse-temurin:17-jre-alpine

# Set environment variables
ENV APP_HOME=/app
ENV JAVA_OPTS="-Xmx512m -Xms256m"  # Optimize memory usage

# Create a working directory
WORKDIR $APP_HOME

# Expose the application port
EXPOSE 8080

# Install curl for health checks
RUN apk add --no-cache curl

# Add a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the JAR file into the container (without renaming)
COPY target/visitor-service-0.0.1-SNAPSHOT.jar visitor-service-0.0.1-SNAPSHOT.jar

# Define a volume for logs (optional)
VOLUME /app/logs

# Fix incorrect permissions
RUN chmod +x visitor-service-0.0.1-SNAPSHOT.jar

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar visitor-service-0.0.1-SNAPSHOT.jar"]
