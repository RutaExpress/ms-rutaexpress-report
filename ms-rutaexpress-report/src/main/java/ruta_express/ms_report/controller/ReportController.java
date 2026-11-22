package ruta_express.ms_report.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ruta_express.ms_report.service.ReportService;

// endpoints de lectura de report (caso, sección 5); los llama el BFF con rol ADMIN
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // GET /kpis?range=last24h: los 3 KPI del caso (estados activos, envíos por hora y lead time)
    @GetMapping("/kpis")
    public Map<String, Object> getKpis(@RequestParam(defaultValue = "last24h") String range) {
        return Map.of(
                "estadosActivos", reportService.getActiveStatusCounts(),
                "enviosPorHora", reportService.getShipmentsPerHour(range),
                "leadTime", reportService.getLeadTime(range));
    }

    // GET /top-services?range=last7d: servicios más usados en el rango
    @GetMapping("/top-services")
    public List<Map<String, Object>> getTopServices(@RequestParam(defaultValue = "last7d") String range) {
        return reportService.getTopServices(range);
    }
}
