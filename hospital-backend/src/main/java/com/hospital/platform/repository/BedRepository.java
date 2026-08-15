package com.hospital.platform.repository;

import com.hospital.platform.entity.Bed;
import com.hospital.platform.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByHospitalId(Long hospitalId);
    List<Bed> findByWardId(Long wardId);
    List<Bed> findByHospitalIdAndStatus(Long hospitalId, Bed.BedStatus status);
    List<Bed> findByHospitalIdAndBedTypeAndStatus(Long hospitalId, Ward.BedType bedType, Bed.BedStatus status);
    
    @Query("SELECT COUNT(b) FROM Bed b WHERE b.hospitalId = :hospitalId AND b.bedType = :bedType AND b.status = 'AVAILABLE'")
    Long countAvailableBedsByType(@Param("hospitalId") Long hospitalId, @Param("bedType") Ward.BedType bedType);
}
