package com.hospital.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_requests")
public class TransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private String patientName;

    @Column(nullable = false)
    private Long sourceHospitalId;

    private Long destinationHospitalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Ward.BedType requiredBedType;

    private String urgency;
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.REQUESTED;

    private LocalDateTime requestTime = LocalDateTime.now();
    private LocalDateTime completedTime;

    public enum TransferStatus {
        REQUESTED, PENDING, ACCEPTED, REJECTED, IN_TRANSIT, COMPLETED, CANCELLED
    }

    public TransferRequest() {}

    public TransferRequest(Long patientId, String patientName, Long sourceHospitalId, Long destinationHospitalId, Ward.BedType requiredBedType, String urgency, String notes) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.sourceHospitalId = sourceHospitalId;
        this.destinationHospitalId = destinationHospitalId;
        this.requiredBedType = requiredBedType;
        this.urgency = urgency;
        this.notes = notes;
        this.status = TransferStatus.REQUESTED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getSourceHospitalId() { return sourceHospitalId; }
    public void setSourceHospitalId(Long sourceHospitalId) { this.sourceHospitalId = sourceHospitalId; }

    public Long getDestinationHospitalId() { return destinationHospitalId; }
    public void setDestinationHospitalId(Long destinationHospitalId) { this.destinationHospitalId = destinationHospitalId; }

    public Ward.BedType getRequiredBedType() { return requiredBedType; }
    public void setRequiredBedType(Ward.BedType requiredBedType) { this.requiredBedType = requiredBedType; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }

    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
}
