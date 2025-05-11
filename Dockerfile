# Этап 1: Сборка приложения
FROM gradle:8.2.1-jdk17 AS builder

WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .

# Используем локальный кеш внутри проекта
ENV GRADLE_USER_HOME=/home/gradle/project/.gradle

RUN gradle build --no-daemon


# Этап 2: Запуск приложения
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
