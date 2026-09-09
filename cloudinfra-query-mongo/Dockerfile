# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy core-api first (dependency) and install it
COPY cloudinfra-core-api /app/cloudinfra-core-api
RUN cd cloudinfra-core-api && ./mvnw -q clean install -DskipTests

# Copy and build the service
COPY cloudinfra-query-mongo /app/cloudinfra-query-mongo
RUN cd cloudinfra-query-mongo && ./mvnw -q clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/cloudinfra-query-mongo/target/*.jar app.jar
EXPOSE 9082
ENTRYPOINT ["java", "-jar", "app.jar"]