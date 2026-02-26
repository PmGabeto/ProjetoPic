# Estágio de Build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# Estágio de Execução
FROM openjdk:17-jdk-slim
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]