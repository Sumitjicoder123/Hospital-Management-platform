package com.hospital.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admissions")
public class
Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private String patientName;

    @Column(nullable = false)
    private Long hospitalId;

    private Long doctorId;
    private Long bedId;

    @Enumerated(EnumType.STRING)
    private Ward.BedType requiredBedType;

    @Enumerated(EnumType.STRING)
    private AdmissionStatus status = AdmissionStatus.REQUESTED;

    private LocalDateTime requestTime = LocalDateTime.now();
    private LocalDateTime admittedTime;
    private LocalDateTime dischargedTime;

    public enum AdmissionStatus {
        REQUESTED, RESERVED, ADMITTED, DISCHARGED, CANCELLED
    }

    public Admission() {}

    public Admission(Long patientId, String patientName, Long hospitalId, Long doctorId, Ward.BedType requiredBedType) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.hospitalId = hospitalId;
        this.doctorId = doctorId;
        this.requiredBedType = requiredBedType;
        this.status = AdmissionStatus.REQUESTED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getBedId() { return bedId; }
    public void setBedId(Long bedId) { this.bedId = bedId; }

    public Ward.BedType getRequiredBedType() { return requiredBedType; }
    public void setRequiredBedType(Ward.BedType requiredBedType) { this.requiredBedType = requiredBedType; }

    public AdmissionStatus getStatus() { return status; }
    public void setStatus(AdmissionStatus status) { this.status = status; }

    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }

    public LocalDateTime getAdmittedTime() { return admittedTime; }
    public void setAdmittedTime(LocalDateTime admittedTime) { this.admittedTime = admittedTime; }

    public LocalDateTime getDischargedTime() { return dischargedTime; }
    public void setDischargedTime(LocalDateTime dischargedTime) { this.dischargedTime = dischargedTime; }
}
