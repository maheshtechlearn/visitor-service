# Use a minimal base image with Java installed
FROM eclipse-temurin:17-jre-alpine

# Set environment variables
ENV APP_HOME=/app
ENV JAVA_OPTS="-Xmx512m -Xms256m"  # Optimize memory usage

# Create a working directory inside the container
WORKDIR $APP_HOME

# Expose the application port
EXPOSE 8080

# Add a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the JAR file into the container
ADD target/visitor-service-0.0.1-SNAPSHOT.jar visitor-service-0.0.1-SNAPSHOT.jar

# Define a volume for logs (optional)
VOLUME /app/logs

# Use a health check to ensure the application is running
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl --fail http://localhost:8080/actuator/health || exit 1

# Set permissions (if needed)
RUN chmod +x app.jar

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

