# --- Compilación (Maven + JDK 21; el código apunta a Java 17) ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Las pruebas usan Testcontainers (necesitan Docker): se corren en desarrollo, no aquí
RUN mvn -q -B package -DskipTests

# --- Ejecución ---
FROM eclipse-temurin:21-jre
ENV TZ=America/Mexico_City
RUN useradd --system --uid 1001 patrimonio
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER patrimonio
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
