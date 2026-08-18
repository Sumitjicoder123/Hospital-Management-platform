package com.hospital.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private Long hospitalId;

    @Column(nullable = false)
    private Long departmentId;

    @Column(nullable = false)
    private String name;

    private String specialization;

    @Enumerated(EnumType.STRING)
    private DoctorStatus status = DoctorStatus.AVAILABLE;

    private Integer consultationAvgTimeMinutes = 15;

    public enum DoctorStatus {
        AVAILABLE, ON_LEAVE, BUSY
    }

    public Doctor() {}

    public Doctor(Long userId, Long hospitalId, Long departmentId, String name, String specialization, DoctorStatus status, Integer consultationAvgTimeMinutes) {
        this.userId = userId;
        this.hospitalId = hospitalId;
        this.departmentId = departmentId;
        this.name = name;
        this.specialization = specialization;
        this.status = status;
        this.consultationAvgTimeMinutes = consultationAvgTimeMinutes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public DoctorStatus getStatus() { return status; }
    public void setStatus(DoctorStatus status) { this.status = status; }

    public Integer getConsultationAvgTimeMinutes() { return consultationAvgTimeMinutes; }
    public void setConsultationAvgTimeMinutes(Integer consultationAvgTimeMinutes) { this.consultationAvgTimeMinutes = consultationAvgTimeMinutes; }
}
