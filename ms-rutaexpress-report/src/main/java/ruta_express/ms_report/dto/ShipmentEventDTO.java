package com.rutaexpress.report.dto;

import java.time.Instant;

public record ShipmentEventDTO(
    String eventId,
    String type,
    Instant timestamp,
    String traceId,
    String correlationId,
    Payload payload
) {
    public record Payload(
        Long shipmentId,
        String trackingNumber,
        String serviceId,
        String customerId,
        String previousStatus,
        String newStatus,
        Instant createdAt
    ) {}
}