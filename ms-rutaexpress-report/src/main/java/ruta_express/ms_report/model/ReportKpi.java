package com.rutaexpress.report.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "report_kpi")
public class ReportKpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant eventTimestamp;
    private String eventType;
    private String newStatus;
    private Long shipmentId;

}