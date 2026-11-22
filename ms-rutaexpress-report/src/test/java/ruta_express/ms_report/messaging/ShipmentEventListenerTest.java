package ruta_express.ms_report.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ruta_express.ms_report.dto.ShipmentEventDTO;
import ruta_express.ms_report.service.ReportService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

// test unitario con Mockito del listener de Kafka (escuchador de eventos de envío)
// recibe el JSON TAL CUAL lo publica shipments (texto, sin headers de tipo) y lo pasa al service ya convertido
@ExtendWith(MockitoExtension.class)
class ShipmentEventListenerTest {

    // dependencia simulada (servicio de reportes)
    @Mock
    private ReportService reportService;

    // clase que se prueba: con un conversor JSON real
    private ShipmentEventListener listener;

    // datos: un evento como el que publica shipments al aceptar el envío 42
    private static final String JSON = """
            {"eventId":"evt-1","type":"SHIPMENT_ACEPTADO","timestamp":"2026-11-21T10:05:00Z",
             "traceId":"trace-1","correlationId":"corr-1",
             "payload":{"shipmentId":42,"trackingNumber":"RX-20261121-ABC123","serviceId":"svc-1",
                        "customerId":"oid-cliente","previousStatus":"CREADO","newStatus":"ACEPTADO",
                        "createdAt":"2026-11-21T10:00:00Z"}}
            """;

    // antes de cada test: el listener con el mock y un JsonMapper real
    @BeforeEach
    void setUp() {
        listener = new ShipmentEventListener(reportService, JsonMapper.builder().build());
    }

    // test 1 (regla principal): el JSON de shipments llega al service convertido en ShipmentEventDTO
    @Test
    @DisplayName("JSON de shipments: llega convertido al service")
    void shipmentsJsonReachesService() {
        // act (actuar)
        listener.onShipmentEvent(JSON);

        // assert (revisar): se entregó un DTO con los datos del JSON
        ArgumentCaptor<ShipmentEventDTO> captor = ArgumentCaptor.forClass(ShipmentEventDTO.class);
        verify(reportService).processShipmentEvent(captor.capture());
        ShipmentEventDTO evento = captor.getValue();
        assertEquals("evt-1", evento.eventId());
        assertEquals(Instant.parse("2026-11-21T10:05:00Z"), evento.timestamp());
        assertEquals(42L, evento.payload().shipmentId());
        assertEquals("CREADO", evento.payload().previousStatus());
        assertEquals("ACEPTADO", evento.payload().newStatus());
    }

    // test 2 (caso de error): un mensaje que no es JSON -> excepción (el error handler lo manda a report.DLT)
    @Test
    @DisplayName("Mensaje que no es JSON: excepción y no llega al service")
    void invalidJsonThrows() {
        assertThrows(JacksonException.class, () -> listener.onShipmentEvent("esto no es json"));
        verifyNoInteractions(reportService);
    }
}
