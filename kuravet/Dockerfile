FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY *.jar app.jar
# Copia o arquivo de config que criamos para a pasta do sistema
COPY src/main/resources/application-docker.properties /app/application-docker.properties

RUN addgroup -S fiapgroup && adduser -S fiapuser -G fiapgroup
USER fiapuser

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application-docker.properties"]