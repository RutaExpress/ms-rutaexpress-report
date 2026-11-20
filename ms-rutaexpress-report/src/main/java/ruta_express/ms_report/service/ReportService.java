package ruta_express.ms_report.service;

import ruta_express.ms_report.dto.ShipmentEventDTO;
import ruta_express.ms_report.model.ReportKpi;
import ruta_express.ms_report.repository.ReportKpiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportKpiRepository repository;

    @Transactional
    public void processShipmentEvent(ShipmentEventDTO event) {
        log.info("Procesando evento para reporte: Envío {} - Nuevo Estado: {}", 
                event.payload().shipmentId(), event.payload().newStatus());

        // Convertimos el DTO a Entidad para guardarlo en Oracle/H2
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
        log.info("Evento {} guardado exitosamente en la base de datos.", event.eventId());
    }

    // Lógica temporal para el endpoint de KPIs
    public Map<String, Long> getActiveStatusCounts() {
        Map<String, Long> kpis = new HashMap<>();
        kpis.put("EN_RUTA", repository.countByNewStatus("EN_RUTA"));
        kpis.put("EN_BODEGA", repository.countByNewStatus("EN_BODEGA"));
        kpis.put("ENTREGADO", repository.countByNewStatus("ENTREGADO"));
        return kpis;
    }
}