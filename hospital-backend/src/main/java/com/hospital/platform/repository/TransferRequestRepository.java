package com.hospital.platform.repository;

import com.hospital.platform.entity.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {
    List<TransferRequest> findBySourceHospitalIdOrDestinationHospitalId(Long sourceId, Long destId);
    List<TransferRequest> findByStatus(TransferRequest.TransferStatus status);
}
