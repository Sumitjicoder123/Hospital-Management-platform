package com.hospital.platform.repository;

import com.hospital.platform.entity.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {
    List<QueueEntry> findByDoctorIdAndStatusIn(Long doctorId, List<QueueEntry.QueueStatus> statuses);
    List<QueueEntry> findByHospitalId(Long hospitalId);
    List<QueueEntry> findByDoctorIdOrderByArrivalTimeAsc(Long doctorId);
    List<QueueEntry> findByPatientPhone(String phone);
    List<QueueEntry> findByPatientId(Long patientId);
    
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.doctorId = :doctorId AND q.status = 'WAITING'")
    Long countWaitingPatientsByDoctor(@Param("doctorId") Long doctorId);
}
