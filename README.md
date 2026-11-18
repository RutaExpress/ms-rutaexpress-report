# ms-rutaexpress-report

Consumidor de eventos de envíos y servicio de KPIs para RutaExpress.

## Estado actual (commit de avance)
- Estructura en capas creada: controller, service, repository, model, dto, messaging, config, exception.
- `ShipmentEventListener` ya escucha `shipments.events` (groupId `report`) y delega en `ReportService`.
- `ReportController` expone los endpoints `/api/v1/report/kpis` y `/top-services` (por ahora devuelven listas vacías).
- Configuración por variables de entorno lista para Oracle y Kafka.

## Puerto
8086

## Variables principales
- `KAFKA_BOOTSTRAP_SERVERS`
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`