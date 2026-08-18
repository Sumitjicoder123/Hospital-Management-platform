package com.hospital.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Patient is identified by patientId (registered users) and/or patientPhone
    // (walk-in / WhatsApp patients who may not have a User account).
    private Long patientId;

    @Column(nullable = false)
    private String patientName;

    private String patientPhone;

    @Column(nullable = false)
    private Long hospitalId;

    @org.hibernate.annotations.Formula("(SELECT h.name FROM hospitals h WHERE h.id = hospital_id)")
    private String hospitalName;

    private Long doctorId;
    private String doctorName;

    private Long departmentId;
    private Long appointmentId;

    @Column(length = 1000)
    private String symptoms;

    @Column(length = 1000)
    private String diagnosis;

    @Column(length = 1500)
    private String prescription;

    @Column(length = 1000)
    private String notes;

    // Simple vitals snapshot taken during this visit
    private String bloodPressure;
    private Double temperatureCelsius;
    private Integer pulseRate;
    private Double weightKg;

    private LocalDateTime visitDate = LocalDateTime.now();
    private LocalDateTime followUpDate;

    public MedicalRecord() {}

    public MedicalRecord(Long patientId, String patientName, String patientPhone, Long hospitalId,
                          Long doctorId, String doctorName, Long departmentId, Long appointmentId) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.hospitalId = hospitalId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.departmentId = departmentId;
        this.appointmentId = appointmentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    public Double getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(Double temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }

    public Integer getPulseRate() { return pulseRate; }
    public void setPulseRate(Integer pulseRate) { this.pulseRate = pulseRate; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }

    public LocalDateTime getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDateTime followUpDate) { this.followUpDate = followUpDate; }
}
