package com.hospital.platform.repository;

import com.hospital.platform.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByHospitalId(Long hospitalId);
}
