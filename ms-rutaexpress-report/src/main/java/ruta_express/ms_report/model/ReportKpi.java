package ruta_express.ms_report.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "report_kpi")
public class ReportKpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String eventId;
    private Long shipmentId;
    private String trackingNumber;
    private String serviceId;
    private String customerId;
    private String previousStatus;
    private String newStatus;
    private Instant eventTimestamp;
    private Instant createdAt;
}