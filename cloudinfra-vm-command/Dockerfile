# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy core-api first (dependency) and install it
COPY cloudinfra-core-api /app/cloudinfra-core-api
RUN cd cloudinfra-core-api && ./mvnw -q clean install -DskipTests

# Copy and build the service
COPY cloudinfra-vm-command /app/cloudinfra-vm-command
RUN cd cloudinfra-vm-command && ./mvnw -q clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/cloudinfra-vm-command/target/*.jar app.jar
EXPOSE 9080
ENTRYPOINT ["java", "-jar", "app.jar"]