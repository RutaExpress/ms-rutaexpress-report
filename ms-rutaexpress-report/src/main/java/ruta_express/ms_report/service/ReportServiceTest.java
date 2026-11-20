package ruta_express.ms_report.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ruta_express.ms_report.dto.ShipmentEventDTO;
import ruta_express.ms_report.model.ReportKpi;
import ruta_express.ms_report.repository.ReportKpiRepository;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportKpiRepository repository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void processShipmentEvent_DebeGuardarEnBaseDeDatos() {
        // Arrange (Preparar el evento)
        ShipmentEventDTO.Payload payload = new ShipmentEventDTO.Payload(
                42L, "RX-123", "SRV-1", "cliente-1", "CREADO", "EN_RUTA", Instant.now()
        );
        ShipmentEventDTO evento = new ShipmentEventDTO(
                "evt-001", "SHIPMENT_ACEPTADO", Instant.now(), "trace-1", "corr-1", payload
        );

        // Act (Ejecutar el servicio)
        reportService.processShipmentEvent(evento);

        // Assert (Verificar que se llamó al repositorio para guardar)
        verify(repository, times(1)).save(any(ReportKpi.class));
    }

    @Test
    void getActiveStatusCounts_DebeRetornarConteosCorrectos() {
        // Arrange
        when(repository.countByNewStatus("EN_RUTA")).thenReturn(5L);
        when(repository.countByNewStatus("EN_BODEGA")).thenReturn(2L);
        when(repository.countByNewStatus("ENTREGADO")).thenReturn(10L);

        // Act
        Map<String, Long> kpis = reportService.getActiveStatusCounts();

        // Assert
        assertEquals(5L, kpis.get("EN_RUTA"));
        assertEquals(2L, kpis.get("EN_BODEGA"));
        assertEquals(10L, kpis.get("ENTREGADO"));
    }
}