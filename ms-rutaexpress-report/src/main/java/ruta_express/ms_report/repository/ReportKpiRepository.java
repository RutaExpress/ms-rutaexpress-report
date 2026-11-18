package com.rutaexpress.report.repository;

import com.rutaexpress.report.model.ReportKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportKpiRepository extends JpaRepository<ReportKpi, Long> {
    
}