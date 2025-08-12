# Etapa 1: Construcción
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
WORKDIR /app
COPY --from=build /app/target/*.jar ProyectoConstruccionDeSoftware.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ProyectoConstruccionDeSoftware.jar"]
