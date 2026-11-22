# ms-rutaexpress-report

Microservicio de **reportería** del caso RutaExpress: consume desde **Kafka** los eventos de `shipments.events` y calcula los KPI del panel de operaciones. Guarda en **Oracle**.

> Arquitectura en capas (rama `refactor/ArquitecturaCapas`).

## Tecnologías
Java 21 · Spring Boot 4.1.2 · Spring Kafka · Spring Data JPA (Oracle) · Lombok · JUnit 5 y Mockito

## Arquitectura en capas
Un evento entra por **messaging → service → repository**; una consulta baja por **controller → service → repository**.

```
src/main/java/ruta_express/ms_report/
├── messaging/    Listener de shipments.events (grupo «report»)
├── controller/   Endpoints de los KPI
├── service/      Cálculo de estados activos, envíos por hora, lead time y servicios más usados
├── repository/   Spring Data JPA: consultas de los KPI
├── model/        Entidad ReportKpi
├── dto/          Evento recibido y respuestas
├── exception/    Manejador global (400 si el rango no existe)
└── config/       Tópicos y manejo de errores de Kafka (3 reintentos y luego report.DLT)
```

## Endpoints (puerto 8086)
| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/v1/report/kpis?range=last24h` | Estados activos, envíos por hora y lead time (`range`: `last24h` o `last7d`) |
| GET | `/api/v1/report/top-services?range=last7d` | Servicios más usados |

## Tests
```
cd ms-rutaexpress-report
mvnw.cmd test
```
