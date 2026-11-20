package ruta_express.ms_report.controller;

import ruta_express.ms_report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/kpis")
    public Map<String, Object> getKpis(@RequestParam(defaultValue = "last24h") String range) {
        return Map.of(
            "estadosActivos", reportService.getActiveStatusCounts(),
            "enviosPorHora", reportService.getShipmentsPerHour(range)
        );
    }

    @GetMapping("/top-services")
    public List<Map<String, Object>> getTopServices(@RequestParam(defaultValue = "last7d") String range) {
        return reportService.getTopServices(range);
    }
}