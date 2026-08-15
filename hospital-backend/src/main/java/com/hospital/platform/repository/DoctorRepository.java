package com.hospital.platform.repository;

import com.hospital.platform.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByHospitalId(Long hospitalId);
    List<Doctor> findByDepartmentId(Long departmentId);
    List<Doctor> findByHospitalIdAndDepartmentId(Long hospitalId, Long departmentId);
}
