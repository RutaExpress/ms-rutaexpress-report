package ruta_express.ms_report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ruta_express.ms_report.dto.LeadTimeResponse;
import ruta_express.ms_report.dto.ShipmentEventDTO;
import ruta_express.ms_report.model.ReportKpi;
import ruta_express.ms_report.repository.ReportKpiRepository;

// test unitario con Mockito del service de reportería (servicio de reportes)
// el repository es un mock: se programa qué devuelve la base y se revisa qué se guardó
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    // dependencia simulada (repositorio de KPI)
    @Mock
    private ReportKpiRepository repository;

    // clase que se prueba, con el mock inyectado por el constructor
    @InjectMocks
    private ReportService reportService;

    // datos: hora en que se creó el envío
    private static final Instant CREADO_EN = Instant.parse("2026-11-21T10:00:00Z");

    // datos: un evento como los que publica shipments en shipments.events
    private ShipmentEventDTO evento(String eventId, String anterior, String nuevo, Instant cuando) {
        return new ShipmentEventDTO(eventId, "SHIPMENT_" + nuevo, cuando, "trace-1", "corr-1",
                new ShipmentEventDTO.Payload(42L, "RX-20261121-ABC123", "svc-1", "oid-cliente", anterior, nuevo, CREADO_EN));
    }

    // test 1 (HU 8, regla principal): un evento nuevo se guarda con los datos del envío
    @Test
    @DisplayName("Evento nuevo: se guarda con los datos del payload")
    void newEventIsSaved() {
        // arrange (preparar): el eventId no está en la base
        when(repository.existsByEventId("evt-1")).thenReturn(false);

        // act (actuar)
        reportService.processShipmentEvent(evento("evt-1", "CREADO", "ACEPTADO", CREADO_EN.plus(5, ChronoUnit.MINUTES)));

        // assert (revisar): se guardó una fila con esos datos
        ArgumentCaptor<ReportKpi> captor = ArgumentCaptor.forClass(ReportKpi.class);
        verify(repository).save(captor.capture());
        ReportKpi kpi = captor.getValue();
        assertEquals("evt-1", kpi.getEventId());
        assertEquals(42L, kpi.getShipmentId());
        assertEquals("ACEPTADO", kpi.getNewStatus());
        assertEquals(CREADO_EN, kpi.getCreatedAt());
    }

    // test 2 (idempotencia): Kafka puede entregar dos veces el mismo evento -> la segunda vez no se guarda
    @Test
    @DisplayName("Evento repetido: no se guarda dos veces")
    void repeatedEventIsIgnored() {
        // arrange (preparar): el eventId ya está guardado
        when(repository.existsByEventId("evt-1")).thenReturn(true);

        // act (actuar)
        reportService.processShipmentEvent(evento("evt-1", "CREADO", "ACEPTADO", CREADO_EN));

        // assert (revisar)
        verify(repository, never()).save(any());
    }

    // test 3 (caso: «estados activos»): cuenta el estado ACTUAL de cada envío, solo los que siguen en curso
    @Test
    @DisplayName("Estados activos: estado actual de cada envío, sin los finales")
    void activeStatusesUseCurrentStatus() {
        // arrange (preparar): la base dice cuántos envíos hay en cada estado actual
        when(repository.countCurrentStatuses()).thenReturn(List.<Object[]>of(
                new Object[] {"EN_RUTA", 2L}, new Object[] {"CREADO", 1L}, new Object[] {"ENTREGADO", 5L}));

        // act (actuar)
        Map<String, Long> activos = reportService.getActiveStatusCounts();

        // assert (revisar): los 4 estados en curso, con 0 si no hay; ENTREGADO es final y no aparece
        assertEquals(Map.of("CREADO", 1L, "ACEPTADO", 0L, "EN_BODEGA", 0L, "EN_RUTA", 2L), activos);
    }

    // test 4 (caso: «lead time»): promedio de minutos entre que se crea y se entrega el envío
    @Test
    @DisplayName("Lead time: promedio de minutos de creación a entrega")
    void leadTimeIsAverageFromCreationToDelivery() {
        // arrange (preparar): dos entregas, una en 120 minutos y otra en 60
        ReportKpi rapida = ReportKpi.builder().newStatus("ENTREGADO").createdAt(CREADO_EN)
                .eventTimestamp(CREADO_EN.plus(60, ChronoUnit.MINUTES)).build();
        ReportKpi lenta = ReportKpi.builder().newStatus("ENTREGADO").createdAt(CREADO_EN)
                .eventTimestamp(CREADO_EN.plus(120, ChronoUnit.MINUTES)).build();
        when(repository.findByNewStatusAndEventTimestampAfter(eq("ENTREGADO"), any(Instant.class)))
                .thenReturn(List.of(rapida, lenta));

        // act (actuar)
        LeadTimeResponse leadTime = reportService.getLeadTime("last7d");

        // assert (revisar)
        assertEquals(2L, leadTime.entregados());
        assertEquals(90.0, leadTime.promedioMinutos());
    }

    // test 5: sin entregas en el rango -> 0 entregados y promedio 0 (no un error)
    @Test
    @DisplayName("Lead time sin entregas: 0")
    void leadTimeWithoutDeliveriesIsZero() {
        // arrange (preparar)
        when(repository.findByNewStatusAndEventTimestampAfter(eq("ENTREGADO"), any(Instant.class))).thenReturn(List.of());

        // act (actuar)
        LeadTimeResponse leadTime = reportService.getLeadTime("last24h");

        // assert (revisar)
        assertEquals(0L, leadTime.entregados());
        assertEquals(0.0, leadTime.promedioMinutos());
    }

    // test 6 (caso de error): un rango que no existe -> IllegalArgumentException (el controller responde 400)
    @Test
    @DisplayName("Rango desconocido: IllegalArgumentException")
    void unknownRangeThrows() {
        assertThrows(IllegalArgumentException.class, () -> reportService.getLeadTime("ayer"));
    }
}
