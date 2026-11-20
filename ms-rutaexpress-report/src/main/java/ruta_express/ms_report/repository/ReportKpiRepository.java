package ruta_express.ms_report.repository;

import ruta_express.ms_report.model.ReportKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportKpiRepository extends JpaRepository<ReportKpi, Long> {
    
    long countByNewStatus(String newStatus);
}