# ms-rutaexpress-report

Microservicio encargado de consumir eventos de envíos desde Kafka y calcular KPIs analíticos para RutaExpress.

## 🚀 Tecnologías
- Java 21 / Spring Boot
- Spring Data JPA + Oracle (H2 en local)
- Spring Kafka
- Maven

## ⚙️ Variables de Entorno
Revisar el archivo `.env.example` para las variables necesarias.

## 🔌 Endpoints Principales
- `GET /api/v1/report/kpis?range=last24h` (Retorna estados activos y envíos por hora)
- `GET /api/v1/report/top-services?range=last7d` (Retorna los servicios más solicitados)
- `GET /actuator/health` (Estado del microservicio para Docker)

## 🏗️ Cómo ejecutar en local
1. Asegurarse de tener Kafka corriendo (o usar el compose de Felipe).
2. Ejecutar `mvn spring-boot:run`.
3. Consumir los endpoints vía `http://localhost:8086`.

## 🐳 Docker
Para construir la imagen:
```bash
docker build -t ms-rutaexpress-report .