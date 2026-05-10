# 1. Aşama: Build (Maven ile derleme)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Testleri atlayarak hızlıca build alıyoruz
RUN mvn clean package -DskipTests

# 2. Aşama: Run (Sadece JAR dosyasını çalıştıran hafif imaj)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Hetzner ARM mimarisi için uygun JRE kullanılacak
ENTRYPOINT ["java", "-jar", "app.jar"]