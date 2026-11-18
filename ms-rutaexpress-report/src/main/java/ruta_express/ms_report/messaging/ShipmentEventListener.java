package com.rutaexpress.report.messaging;

import com.rutaexpress.report.config.KafkaTopics;
import com.rutaexpress.report.dto.ShipmentEventDTO;
import com.rutaexpress.report.service.ReportService;
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