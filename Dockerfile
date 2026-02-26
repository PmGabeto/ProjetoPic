# Estágio de Build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
# --no-daemon poupa memória na VPS durante o build
RUN gradle build --no-daemon -x test

# Estágio de Execução (Substituído openjdk por amazoncorretto)
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
EXPOSE 8080

# Copia o jar gerado no estágio anterior
# Nota: Verifique se o caminho build/libs/ está correto no seu projeto
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]