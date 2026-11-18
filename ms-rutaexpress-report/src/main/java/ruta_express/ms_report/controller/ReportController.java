package com.rutaexpress.report.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @GetMapping("/kpis")
    public List<Map<String, Object>> getKpis(@RequestParam(defaultValue = "last24h") String range) {
        // TODO paso 2: consultar datos reales
        return List.of();
    }

    @GetMapping("/top-services")
    public List<Map<String, Object>> getTopServices(@RequestParam(defaultValue = "last7d") String range) {
        return List.of();
    }
}