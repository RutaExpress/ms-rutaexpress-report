package ruta_express.ms_report.messaging;

import ruta_express.ms_report.config.KafkaTopics;
import ruta_express.ms_report.dto.ShipmentEventDTO;
import ruta_express.ms_report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEventListener {

    private final ReportService reportService;

    @KafkaListener(topics = KafkaTopics.SHIPMENTS_EVENTS, groupId = "report")
    public void onShipmentEvent(ShipmentEventDTO event) {
        reportService.processShipmentEvent(event);
    }
}