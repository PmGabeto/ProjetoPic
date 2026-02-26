# Estágio de Build - Gradle 8.12 com suporte total ao Java 21
FROM gradle:8.12-jdk21 AS build
WORKDIR /app
COPY . .

# Otimização: O Gradle 8.12 gerencia melhor o cache de dependências
RUN gradle build --no-daemon -x test

# Estágio de Execução
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
EXPOSE 8080

# Copia o jar gerado.
# Como seu projeto se chama 'swapi' no log, o padrão é swapi-0.0.1-SNAPSHOT.jar
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]