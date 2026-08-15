package com.hospital.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "wards")
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hospitalId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedType type;

    private Integer capacity = 10;

    public enum BedType {
        GENERAL, ICU, EMERGENCY, HDU, PEDIATRIC, MATERNITY, ISOLATION
    }

    public Ward() {}

    public Ward(Long hospitalId, String name, BedType type, Integer capacity) {
        this.hospitalId = hospitalId;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BedType getType() { return type; }
    public void setType(BedType type) { this.type = type; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
