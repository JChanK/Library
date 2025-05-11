# Этап 1: Сборка
FROM gradle:8.2.1-jdk17 AS builder

USER root
WORKDIR /home/gradle/project
COPY . .

# Установка прав
RUN mkdir -p /home/gradle/project/.gradle && \
    chown -R gradle:gradle /home/gradle/project

ENV GRADLE_USER_HOME=/home/gradle/project/.gradle
USER gradle

RUN gradle build --no-daemon --no-build-cache --refresh-dependencies

# Этап 2: Запуск
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
