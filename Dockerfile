# Start with a base image containing Java runtime
FROM openjdk:11-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file into the container
COPY target/*.jar spring-cloud-client-demo-0.0.1-SNAPSHOT.jar

# Expose the port your Spring Boot app runs on
EXPOSE 3330

# Run the jar file
ENTRYPOINT ["java", "-jar", "spring-cloud-client-demo-0.0.1-SNAPSHOT.jar"]