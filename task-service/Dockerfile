# Контекст сборки — корень репозитория (многомодульный Gradle): docker build -f task-service/Dockerfile .

# ---- build ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

# сначала только файлы сборки — зависимости кэшируются отдельным слоем
COPY gradlew settings.gradle.kts gradle.properties lombok.config ./
COPY gradle gradle
COPY task-service/build.gradle.kts task-service/
COPY auth-service/build.gradle.kts auth-service/
RUN ./gradlew --no-daemon :task-service:dependencies > /dev/null 2>&1 || true

COPY task-service/src task-service/src
RUN ./gradlew --no-daemon :task-service:bootJar -x test

ARG JAR_FILE=task-service/build/*.jar
ARG JAR_NAME=app.jar

RUN cp $JAR_FILE /$JAR_NAME

# ---- runtime ----
FROM eclipse-temurin:21-jre AS run

RUN groupadd --system app && useradd --system --gid app app

WORKDIR /

ARG JAR_NAME=app.jar

ENV APP_NAME task-service
ENV LANG C.UTF-8
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

COPY --from=build /$JAR_NAME /$JAR_NAME

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
