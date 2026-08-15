package com.hospital.platform.repository;

import com.hospital.platform.entity.Admission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {
    List<Admission> findByHospitalId(Long hospitalId);
    List<Admission> findByPatientId(Long patientId);
}
