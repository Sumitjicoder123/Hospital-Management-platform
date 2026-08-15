package com.hospital.platform.repository;

import com.hospital.platform.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByHospitalId(Long hospitalId);
    List<Appointment> findByPatientPhone(String phone);
}
