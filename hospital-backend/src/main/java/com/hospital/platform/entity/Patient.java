package com.hospital.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Authoritative patient record. A patient is identified primarily by phone
 * number (normalized to digits-only) so that the same person is recognized
 * across web bookings, WhatsApp bookings, and repeat hospital visits, even if
 * they never created a login account.
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Normalized: digits only, no spaces/dashes/+prefix. This is the actual
    // join key used to recognize a returning patient.
    @Column(nullable = false)
    private String phone;

    private String email;
    private Integer age;
    private String gender;
    private String bloodGroup;

    // Optional link to a registered User account, if the patient ever signs up.
    private Long userId;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Patient() {}

    public Patient(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
