# Etapa 1: Construcción
FROM maven:3.9.5-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiamos el JAR generado
COPY --from=builder /app/target/*.jar app.jar

# Puerto del microservicio
EXPOSE 8086

# Healthcheck (usa el Actuator que agregamos al pom)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8086/actuator/health || exit 1

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]