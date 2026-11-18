package com.rutaexpress.report.service;

import com.rutaexpress.report.dto.ShipmentEventDTO;
import com.rutaexpress.report.repository.ReportKpiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportKpiRepository repository;

    @Transactional
    public void processShipmentEvent(ShipmentEventDTO event) {
        log.info("Recibido evento de envío: {} - tipo: {}", event.payload().shipmentId(), event.type());
        // TODO paso 2: persistir en Oracle y calcular KPIs
    }
}