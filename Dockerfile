# Paso 1: Construir la aplicación usando Maven
FROM maven:3.8.5-openjdk-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Paso 2: Ejecutar la aplicación usando OpenJDK
FROM openjdk:21-jdk-slim
COPY --from=build /target/helpdesk-backend-0.0.1-SNAPSHOT.jar helpdesk-backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","helpdesk-backend.jar"]