# Базовый образ с Java 17
FROM eclipse-temurin:17-jdk-jammy

# Рабочая директория в контейнере
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY build/libs/Library-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт, который использует Spring Boot (по умолчанию 8080)
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]