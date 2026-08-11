# Contexto de build: raiz del repositorio (ver docker-compose.yaml)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY delivery-contracts ./delivery-contracts
RUN mvn -q -f delivery-contracts/pom.xml install -DskipTests
COPY comercio-service ./service
RUN mvn -q -f service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
