# Imagen del backend para el deploy (Render, Railway, Fly.io, etc.)
# Construir:  docker build -t adoptar-api ./backend

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecutar (imagen liviana, solo el JRE) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Perfil de producción: NO carga los datos de prueba
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
