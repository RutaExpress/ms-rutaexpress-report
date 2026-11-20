package ruta_express.ms_report.service;

import ruta_express.ms_report.dto.ShipmentEventDTO;
import ruta_express.ms_report.model.ReportKpi;
import ruta_express.ms_report.repository.ReportKpiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportKpiRepository repository;

    @Transactional
    public void processShipmentEvent(ShipmentEventDTO event) {
        log.info("Procesando evento: Envío {} - Estado: {}", event.payload().shipmentId(), event.payload().newStatus());
        ReportKpi kpi = ReportKpi.builder()
                .eventId(event.eventId())
                .shipmentId(event.payload().shipmentId())
                .trackingNumber(event.payload().trackingNumber())
                .serviceId(event.payload().serviceId())
                .customerId(event.payload().customerId())
                .previousStatus(event.payload().previousStatus())
                .newStatus(event.payload().newStatus())
                .eventTimestamp(event.timestamp())
                .createdAt(event.payload().createdAt())
                .build();
        repository.save(kpi);
    }

    public Map<String, Long> getActiveStatusCounts() {
        Map<String, Long> kpis = new HashMap<>();
        kpis.put("EN_RUTA", repository.countByNewStatus("EN_RUTA"));
        kpis.put("EN_BODEGA", repository.countByNewStatus("EN_BODEGA"));
        kpis.put("ENTREGADO", repository.countByNewStatus("ENTREGADO"));
        return kpis;
    }

    public Map<String, Long> getShipmentsPerHour(String range) {
        Instant startTime = range.equals("last24h") ? 
                Instant.now().minus(24, ChronoUnit.HOURS) : 
                Instant.now().minus(7, ChronoUnit.DAYS);
        
        List<ReportKpi> events = repository.findByEventTimestampAfter(startTime);
        
        // Agrupamos por la hora del evento (ej. "14:00")
        return events.stream()
                .filter(e -> e.getEventTimestamp() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getEventTimestamp().toString().substring(11, 13) + ":00", 
                        Collectors.counting()
                ));
    }

    public List<Map<String, Object>> getTopServices(String range) {
        return repository.findTopServices().stream()
                .map(row -> Map.of("serviceId", row[0], "count", row[1]))
                .collect(Collectors.toList());
    }
}