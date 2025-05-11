FROM gradle:8.2.1-jdk17 AS builder

# Запускаем от root
USER root

# Создаём рабочую директорию и кеш вручную
WORKDIR /home/gradle/project
COPY . .

# Удаляем потенциально битый кэш, настраиваем права и окружение
RUN rm -rf /home/gradle/.gradle && \
    mkdir -p /home/gradle/.gradle && \
    chown -R gradle:gradle /home/gradle && \
    chown -R gradle:gradle /home/gradle/project

# Указываем нестандартное место для GRADLE_USER_HOME
ENV GRADLE_USER_HOME=/home/gradle/.gradle

USER gradle

# Сборка проекта
RUN gradle clean build --no-daemon --no-build-cache --refresh-dependencies


# Этап 2: Запуск
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
