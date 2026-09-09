# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy core-api first (dependency) and install it
COPY cloudinfra-core-api /app/cloudinfra-core-api
RUN cd cloudinfra-core-api && ./mvnw -q clean install -DskipTests

# Copy and build the service
COPY cloudinfra-dc-command /app/cloudinfra-dc-command
RUN cd cloudinfra-dc-command && ./mvnw -q clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/cloudinfra-dc-command/target/*.jar app.jar
EXPOSE 9081
ENTRYPOINT ["java", "-jar", "app.jar"]