package com.hospital.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "beds")
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long wardId;

    @Column(nullable = false)
    private Long hospitalId;

    @Column(nullable = false)
    private String bedNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Ward.BedType bedType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedStatus status = BedStatus.AVAILABLE;

    private Long currentPatientId;
    private String currentPatientName;

    @Version
    private Long version;

    public enum BedStatus {
        AVAILABLE, RESERVED, OCCUPIED, CLEANING, MAINTENANCE, BLOCKED
    }

    public Bed() {}

    public Bed(Long wardId, Long hospitalId, String bedNumber, Ward.BedType bedType, BedStatus status) {
        this.wardId = wardId;
        this.hospitalId = hospitalId;
        this.bedNumber = bedNumber;
        this.bedType = bedType;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWardId() { return wardId; }
    public void setWardId(Long wardId) { this.wardId = wardId; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public Ward.BedType getBedType() { return bedType; }
    public void setBedType(Ward.BedType bedType) { this.bedType = bedType; }

    public BedStatus getStatus() { return status; }
    public void setStatus(BedStatus status) { this.status = status; }

    public Long getCurrentPatientId() { return currentPatientId; }
    public void setCurrentPatientId(Long currentPatientId) { this.currentPatientId = currentPatientId; }

    public String getCurrentPatientName() { return currentPatientName; }
    public void setCurrentPatientName(String currentPatientName) { this.currentPatientName = currentPatientName; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
