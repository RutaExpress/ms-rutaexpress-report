package ruta_express.ms_report.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ruta_express.ms_report.config.KafkaTopics;
import ruta_express.ms_report.dto.ShipmentEventDTO;
import ruta_express.ms_report.service.ReportService;
import tools.jackson.databind.ObjectMapper;

// consumidor de shipments.events (group report): recibe el JSON como texto y lo convierte aquí
// shipments publica sin headers de tipo, así que el listener decide a qué clase convertirlo
@Component
@RequiredArgsConstructor
public class ShipmentEventListener {

    // dependencias: el service que guarda el evento y el conversor JSON de Spring
    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    // onShipmentEvent (al llegar un evento): JSON -> ShipmentEventDTO -> service
    // si el JSON está mal, la excepción la toma el error handler y el mensaje termina en report.DLT
    @KafkaListener(topics = KafkaTopics.SHIPMENTS_EVENTS, groupId = "report")
    public void onShipmentEvent(String json) {
        reportService.processShipmentEvent(objectMapper.readValue(json, ShipmentEventDTO.class));
    }
}
