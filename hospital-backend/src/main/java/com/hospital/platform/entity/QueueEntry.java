package com.hospital.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entries")
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appointmentId;
    private Long patientId;
    private String tokenNumber;
    private String patientName;
    private String patientPhone;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private Long hospitalId;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    private LocalDateTime arrivalTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private QueueStatus status = QueueStatus.WAITING;

    private Integer estimatedWaitMinutes = 0;

    private LocalDateTime expectedConsultTime;

    public enum Priority {
        EMERGENCY, CRITICAL, HIGH, NORMAL, ROUTINE
    }

    public enum QueueStatus {
        WAITING, CALLED, IN_CONSULTATION, COMPLETED, CANCELLED, NO_SHOW
    }

    public QueueEntry() {}

    public QueueEntry(Long appointmentId, String tokenNumber, String patientName, String patientPhone, Long doctorId, Long hospitalId, Priority priority) {
        this.appointmentId = appointmentId;
        this.tokenNumber = tokenNumber;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.doctorId = doctorId;
        this.hospitalId = hospitalId;
        this.priority = priority;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public QueueStatus getStatus() { return status; }
    public void setStatus(QueueStatus status) { this.status = status; }

    public Integer getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }

    public LocalDateTime getExpectedConsultTime() { return expectedConsultTime; }
    public void setExpectedConsultTime(LocalDateTime expectedConsultTime) { this.expectedConsultTime = expectedConsultTime; }
}
