package com.hospital.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private Double latitude;
    private Double longitude;
    private String contactPhone;
    private Boolean emergencyAvailable = true;

    @Enumerated(EnumType.STRING)
    private HospitalStatus status = HospitalStatus.NORMAL;

    private Integer totalBeds = 0;
    private Integer occupiedBeds = 0;
    private Integer availableBeds = 0;

    private Integer totalIcuBeds = 0;
    private Integer occupiedIcuBeds = 0;
    private Integer availableIcuBeds = 0;

    private Integer totalEmergencyBeds = 0;
    private Integer occupiedEmergencyBeds = 0;
    private Integer availableEmergencyBeds = 0;

    public enum HospitalStatus {
        NORMAL, HIGH_OCCUPANCY, CRITICAL
    }

    public Hospital() {}

    public Hospital(String name, String address, Double latitude, Double longitude, String contactPhone, Boolean emergencyAvailable) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactPhone = contactPhone;
        this.emergencyAvailable = emergencyAvailable;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public Boolean getEmergencyAvailable() { return emergencyAvailable; }
    public void setEmergencyAvailable(Boolean emergencyAvailable) { this.emergencyAvailable = emergencyAvailable; }

    public HospitalStatus getStatus() { return status; }
    public void setStatus(HospitalStatus status) { this.status = status; }

    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }

    public Integer getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(Integer occupiedBeds) { this.occupiedBeds = occupiedBeds; }

    public Integer getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Integer availableBeds) { this.availableBeds = availableBeds; }

    public Integer getTotalIcuBeds() { return totalIcuBeds; }
    public void setTotalIcuBeds(Integer totalIcuBeds) { this.totalIcuBeds = totalIcuBeds; }

    public Integer getOccupiedIcuBeds() { return occupiedIcuBeds; }
    public void setOccupiedIcuBeds(Integer occupiedIcuBeds) { this.occupiedIcuBeds = occupiedIcuBeds; }

    public Integer getAvailableIcuBeds() { return availableIcuBeds; }
    public void setAvailableIcuBeds(Integer availableIcuBeds) { this.availableIcuBeds = availableIcuBeds; }

    public Integer getTotalEmergencyBeds() { return totalEmergencyBeds; }
    public void setTotalEmergencyBeds(Integer totalEmergencyBeds) { this.totalEmergencyBeds = totalEmergencyBeds; }

    public Integer getOccupiedEmergencyBeds() { return occupiedEmergencyBeds; }
    public void setOccupiedEmergencyBeds(Integer occupiedEmergencyBeds) { this.occupiedEmergencyBeds = occupiedEmergencyBeds; }

    public Integer getAvailableEmergencyBeds() { return availableEmergencyBeds; }
    public void setAvailableEmergencyBeds(Integer availableEmergencyBeds) { this.availableEmergencyBeds = availableEmergencyBeds; }
}
