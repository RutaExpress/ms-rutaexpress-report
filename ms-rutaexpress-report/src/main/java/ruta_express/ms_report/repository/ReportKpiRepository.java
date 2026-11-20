package ruta_express.ms_report.repository;

import ruta_express.ms_report.model.ReportKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReportKpiRepository extends JpaRepository<ReportKpi, Long> {
    
    long countByNewStatus(String newStatus);

    // Trae los eventos desde una fecha (para calcular envíos por hora)
    List<ReportKpi> findByEventTimestampAfter(Instant timestamp);

    // Agrupa y cuenta los servicios más usados (Top Services)
    @Query("SELECT r.serviceId, COUNT(r) FROM ReportKpi r GROUP BY r.serviceId ORDER BY COUNT(r) DESC")
    List<Object[]> findTopServices();
}