package ruta_express.ms_report.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ruta_express.ms_report.model.ReportKpi;

// acceso a los eventos guardados (una fila por evento de shipments.events)
@Repository
public interface ReportKpiRepository extends JpaRepository<ReportKpi, Long> {

    // existsByEventId (¿existe este evento?): para no guardar dos veces el mismo evento (idempotencia)
    boolean existsByEventId(String eventId);

    // eventos de un estado desde una fecha (CREADO para envíos por hora, ENTREGADO para el lead time)
    List<ReportKpi> findByNewStatusAndEventTimestampAfter(String newStatus, Instant since);

    // estado ACTUAL de cada envío: su evento más reciente, contado por estado -> filas [estado, cantidad]
    @Query("""
            SELECT r.newStatus, COUNT(r) FROM ReportKpi r
            WHERE r.eventTimestamp = (SELECT MAX(r2.eventTimestamp) FROM ReportKpi r2 WHERE r2.shipmentId = r.shipmentId)
            GROUP BY r.newStatus
            """)
    List<Object[]> countCurrentStatuses();

    // servicios más usados: envíos CREADOS por servicio desde una fecha -> filas [serviceId, cantidad]
    @Query("""
            SELECT r.serviceId, COUNT(r) FROM ReportKpi r
            WHERE r.newStatus = 'CREADO' AND r.eventTimestamp >= :since
            GROUP BY r.serviceId ORDER BY COUNT(r) DESC
            """)
    List<Object[]> findTopServicesSince(@Param("since") Instant since);
}
