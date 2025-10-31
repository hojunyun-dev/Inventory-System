# Build stage (multi-module aware)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
# Copy entire repository for proper multi-module resolution
COPY .. /build
# Build all modules to ensure parent/common are available
RUN mvn -q -DskipTests install

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /build/backend/target/inventory-system-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]


