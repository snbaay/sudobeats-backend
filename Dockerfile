# ==================== СТАДИЯ СБОРКИ ====================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем pom и скачиваем зависимости (чтобы кэшировалось)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем
COPY src ./src
RUN mvn clean package -DskipTests

# ==================== СТАДИЯ ЗАПУСКА ====================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем только готовый JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Оптимизированный запуск
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]