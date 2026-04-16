# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/techcup-futbol-0.0.1-SNAPSHOT.jar app.jar

ENV DB_URL=jdbc:postgresql://localhost:5433/techcup \
    DB_USERNAME=techcup \
    DB_PASSWORD=techcup \
    JWT_SECRET=dGVjaGN1cC1mdXRib2wtc2VjcmV0LWtleS1mb3ItZGV2ZWxvcG1lbnQ= \
    SSL_KEY_STORE_PASSWORD=techcup123 \
    GOOGLE_CLIENT_ID=dev-client-id \
    GOOGLE_CLIENT_SECRET=dev-client-secret

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
