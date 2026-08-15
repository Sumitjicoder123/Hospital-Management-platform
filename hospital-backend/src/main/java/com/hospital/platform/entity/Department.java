package com.hospital.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hospitalId;

    @Column(nullable = false)
    private String name;

    private String code;

    public Department() {}

    public Department(Long hospitalId, String name, String code) {
        this.hospitalId = hospitalId;
        this.name = name;
        this.code = code;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
