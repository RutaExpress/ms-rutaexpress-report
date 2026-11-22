package ruta_express.ms_report.service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ruta_express.ms_report.dto.LeadTimeResponse;
import ruta_express.ms_report.dto.ShipmentEventDTO;
import ruta_express.ms_report.model.ReportKpi;
import ruta_express.ms_report.repository.ReportKpiRepository;

// reglas de la reportería: guarda los eventos de shipments.events y calcula los KPI del caso
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    // estados en curso (no finales): los que cuenta «estados activos»
    private static final List<String> ESTADOS_ACTIVOS = List.of("CREADO", "ACEPTADO", "EN_BODEGA", "EN_RUTA");

    // hora de un evento como "14:00" (en UTC, igual que el timestamp del envelope)
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:00").withZone(ZoneOffset.UTC);

    // dependencia: la entrega Spring por el constructor que escribe Lombok
    private final ReportKpiRepository repository;

    // processShipmentEvent (procesar evento): guarda una fila por evento; si ya llegó antes, no hace nada
    @Transactional
    public void processShipmentEvent(ShipmentEventDTO event) {
        if (repository.existsByEventId(event.eventId())) {
            log.info("[REPORT] evento repetido, se ignora: {}", event.eventId());
            return;
        }
        ShipmentEventDTO.Payload p = event.payload();
        log.info("[REPORT] envío {} pasa a {}", p.shipmentId(), p.newStatus());
        repository.save(ReportKpi.builder()
                .eventId(event.eventId())
                .shipmentId(p.shipmentId())
                .trackingNumber(p.trackingNumber())
                .serviceId(p.serviceId())
                .customerId(p.customerId())
                .previousStatus(p.previousStatus())
                .newStatus(p.newStatus())
                .eventTimestamp(event.timestamp())
                .createdAt(p.createdAt())
                .build());
    }

    // getActiveStatusCounts (estados activos): cuántos envíos hay HOY en cada estado en curso (0 si no hay)
    public Map<String, Long> getActiveStatusCounts() {
        Map<String, Long> activos = new LinkedHashMap<>();
        ESTADOS_ACTIVOS.forEach(estado -> activos.put(estado, 0L));
        for (Object[] fila : repository.countCurrentStatuses()) {
            String estado = (String) fila[0];
            if (activos.containsKey(estado)) {          // los finales (ENTREGADO, CANCELADO) no se cuentan
                activos.put(estado, (Long) fila[1]);
            }
        }
        return activos;
    }

    // getShipmentsPerHour (envíos por hora): envíos creados en el rango, agrupados por hora
    public Map<String, Long> getShipmentsPerHour(String range) {
        return repository.findByNewStatusAndEventTimestampAfter("CREADO", rangeStart(range)).stream()
                .filter(e -> e.getEventTimestamp() != null)
                .collect(Collectors.groupingBy(e -> HORA.format(e.getEventTimestamp()), TreeMap::new, Collectors.counting()));
    }

    // getLeadTime (tiempo de entrega): promedio de minutos entre la creación y la entrega, en el rango
    public LeadTimeResponse getLeadTime(String range) {
        List<Long> minutos = repository.findByNewStatusAndEventTimestampAfter("ENTREGADO", rangeStart(range)).stream()
                .filter(e -> e.getCreatedAt() != null && e.getEventTimestamp() != null)
                .map(e -> Duration.between(e.getCreatedAt(), e.getEventTimestamp()).toMinutes())
                .toList();
        double promedio = minutos.stream().mapToLong(Long::longValue).average().orElse(0.0);
        return new LeadTimeResponse(minutos.size(), promedio);
    }

    // getTopServices (servicios más usados): envíos creados por servicio en el rango, de mayor a menor
    public List<Map<String, Object>> getTopServices(String range) {
        return repository.findTopServicesSince(rangeStart(range)).stream()
                .filter(fila -> Objects.nonNull(fila[0]))
                .map(fila -> Map.<String, Object>of("serviceId", fila[0], "count", fila[1]))
                .toList();
    }

    // rangeStart (inicio del rango): los rangos del caso; otro valor -> 400 en el controller
    private Instant rangeStart(String range) {
        return switch (range) {
            case "last24h" -> Instant.now().minus(24, ChronoUnit.HOURS);
            case "last7d" -> Instant.now().minus(7, ChronoUnit.DAYS);
            default -> throw new IllegalArgumentException("Rango no soportado: " + range);
        };
    }
}
