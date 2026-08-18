package com.hospital.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private String patientName;
    private String patientPhone;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private Long departmentId;

    @Column(nullable = false)
    private Long hospitalId;

    private LocalDateTime appointmentTime;

    @Enumerated(EnumType.STRING)
    private BookingChannel bookingChannel = BookingChannel.WEB;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    public enum BookingChannel {
        WEB, WHATSAPP
    }

    public enum AppointmentStatus {
        SCHEDULED, BOOKED, CHECKED_IN, IN_CONSULTATION, COMPLETED, CANCELLED
    }

    public Appointment() {}

    public Appointment(Long patientId, String patientName, String patientPhone, Long doctorId, Long departmentId, Long hospitalId, LocalDateTime appointmentTime, BookingChannel bookingChannel) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.doctorId = doctorId;
        this.departmentId = departmentId;
        this.hospitalId = hospitalId;
        this.appointmentTime = appointmentTime;
        this.bookingChannel = bookingChannel;
        this.status = AppointmentStatus.BOOKED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public BookingChannel getBookingChannel() { return bookingChannel; }
    public void setBookingChannel(BookingChannel bookingChannel) { this.bookingChannel = bookingChannel; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
