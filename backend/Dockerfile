# ---------- Stage 1: build the Spring Boot jar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: small runtime image ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/ecommerce-backend-*.jar app.jar

# Keep memory low enough for Render's free instance (512 MB)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"

# Render sets PORT automatically; the app reads it (server.port=${PORT:8081})
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
