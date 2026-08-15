package com.hospital.platform.repository;

import com.hospital.platform.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByHospitalId(Long hospitalId);
    List<Ward> findByHospitalIdAndType(Long hospitalId, Ward.BedType type);
}
