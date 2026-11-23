# ---- build ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

# сначала только файлы сборки — зависимости кэшируются отдельным слоем
COPY gradlew settings.gradle.kts build.gradle.kts lombok.config ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

ARG JAR_FILE=build/*.jar
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
