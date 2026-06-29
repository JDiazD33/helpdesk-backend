# Paso 1: Construir la aplicación usando Maven con Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Paso 2: Ejecutar la aplicación usando Java 21 estable
FROM eclipse-temurin:21-jre-jammy
COPY --from=build /target/helpdesk-backend-0.0.1-SNAPSHOT.jar helpdesk-backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","helpdesk-backend.jar"]