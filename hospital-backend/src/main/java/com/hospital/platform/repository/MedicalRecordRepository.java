package com.hospital.platform.repository;

import com.hospital.platform.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientIdOrderByVisitDateDesc(Long patientId);
    List<MedicalRecord> findByPatientPhoneOrderByVisitDateDesc(String patientPhone);
    List<MedicalRecord> findByHospitalIdOrderByVisitDateDesc(Long hospitalId);
    List<MedicalRecord> findByAppointmentId(Long appointmentId);
}
