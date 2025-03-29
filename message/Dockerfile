FROM maven:3.8.6 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests
FROM openjdk:17-jdk-slim

RUN useradd -m appuser
USER appuser

COPY --from=build /app/target/*.jar /app/notifications-service.jar

EXPOSE 9010
ENTRYPOINT ["java", "-jar", "/app/notifications-service.jar"]


