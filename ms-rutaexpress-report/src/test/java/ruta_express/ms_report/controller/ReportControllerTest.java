package ruta_express.ms_report.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ruta_express.ms_report.dto.LeadTimeResponse;
import ruta_express.ms_report.service.ReportService;

// test de la capa web de report: /kpis responde los 3 KPI del caso y un rango inválido da 400
@WebMvcTest(ReportController.class)
class ReportControllerTest {

    // simulador de peticiones HTTP
    @Autowired
    private MockMvc mockMvc;

    // dependencia simulada (servicio de reportes)
    @MockitoBean
    private ReportService reportService;

    // test 1 (HU 8, caso sección 1): /kpis trae estados activos, envíos por hora y lead time
    @Test
    @DisplayName("GET /kpis: estados activos, envíos por hora y lead time")
    void kpisIncludeTheThreeCaseKpis() throws Exception {
        // arrange (preparar)
        when(reportService.getActiveStatusCounts()).thenReturn(Map.of("EN_RUTA", 2L));
        when(reportService.getShipmentsPerHour("last24h")).thenReturn(Map.of("10:00", 3L));
        when(reportService.getLeadTime("last24h")).thenReturn(new LeadTimeResponse(2L, 90.0));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/report/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadosActivos.EN_RUTA").value(2))
                .andExpect(jsonPath("$.enviosPorHora['10:00']").value(3))
                .andExpect(jsonPath("$.leadTime.entregados").value(2))
                .andExpect(jsonPath("$.leadTime.promedioMinutos").value(90.0));
    }

    // test 2 (caso de error, contrato 5): rango que no existe -> 400 con ProblemDetail
    @Test
    @DisplayName("GET /kpis?range=ayer: 400")
    void unknownRangeGets400() throws Exception {
        // arrange (preparar)
        when(reportService.getShipmentsPerHour("ayer")).thenThrow(new IllegalArgumentException("Rango no soportado: ayer"));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/report/kpis").param("range", "ayer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Rango no soportado: ayer"));
    }
}
