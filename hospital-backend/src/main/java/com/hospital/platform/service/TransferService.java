package com.hospital.platform.service;

import com.hospital.platform.entity.*;
import com.hospital.platform.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferService {

    private final TransferRequestRepository transferRequestRepository;
    private final BedRepository bedRepository;
    private final BedService bedService;
    private final SimpMessagingTemplate messagingTemplate;

    public TransferService(TransferRequestRepository transferRequestRepository,
                           BedRepository bedRepository,
                           BedService bedService,
                           SimpMessagingTemplate messagingTemplate) {
        this.transferRequestRepository = transferRequestRepository;
        this.bedRepository = bedRepository;
        this.bedService = bedService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<TransferRequest> getAllTransfers() {
        return transferRequestRepository.findAll();
    }

    public List<TransferRequest> getTransfersForHospital(Long hospitalId) {
        return transferRequestRepository.findBySourceHospitalIdOrDestinationHospitalId(hospitalId, hospitalId);
    }

    @Transactional
    public TransferRequest initiateTransfer(Long patientId, String patientName, Long sourceHospitalId, Long destinationHospitalId, Ward.BedType requiredBedType, String urgency, String notes) {
        TransferRequest request = new TransferRequest(
                patientId,
                patientName,
                sourceHospitalId,
                destinationHospitalId,
                requiredBedType,
                urgency,
                notes
        );
        request.setStatus(TransferRequest.TransferStatus.PENDING);
        TransferRequest saved = transferRequestRepository.save(request);

        broadcastTransferUpdate();
        return saved;
    }

    @Transactional
    public TransferRequest acceptTransfer(Long transferId) {
        TransferRequest request = transferRequestRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer request not found: " + transferId));

        request.setStatus(TransferRequest.TransferStatus.ACCEPTED);

        // Reserve bed at destination hospital
        List<Bed> availBeds = bedRepository.findByHospitalIdAndBedTypeAndStatus(
                request.getDestinationHospitalId(), request.getRequiredBedType(), Bed.BedStatus.AVAILABLE
        );

        if (!availBeds.isEmpty()) {
            Bed bed = availBeds.get(0);
            bedService.reserveBed(bed.getId(), request.getPatientId(), request.getPatientName());
        }

        TransferRequest saved = transferRequestRepository.save(request);
        broadcastTransferUpdate();
        return saved;
    }

    @Transactional
    public TransferRequest rejectTransfer(Long transferId) {
        TransferRequest request = transferRequestRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer request not found: " + transferId));

        request.setStatus(TransferRequest.TransferStatus.REJECTED);
        TransferRequest saved = transferRequestRepository.save(request);

        broadcastTransferUpdate();
        return saved;
    }

    @Transactional
    public TransferRequest updateTransferStatus(Long transferId, TransferRequest.TransferStatus newStatus) {
        TransferRequest request = transferRequestRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer request not found: " + transferId));

        request.setStatus(newStatus);
        if (newStatus == TransferRequest.TransferStatus.COMPLETED) {
            request.setCompletedTime(LocalDateTime.now());
            // Set reserved bed at destination hospital to OCCUPIED
            List<Bed> reservedBeds = bedRepository.findByHospitalIdAndStatus(request.getDestinationHospitalId(), Bed.BedStatus.RESERVED);
            for (Bed b : reservedBeds) {
                if (request.getPatientId() != null && request.getPatientId().equals(b.getCurrentPatientId())) {
                    bedService.updateBedStatus(b.getId(), Bed.BedStatus.OCCUPIED);
                    break;
                }
            }
        }

        TransferRequest saved = transferRequestRepository.save(request);
        broadcastTransferUpdate();
        return saved;
    }

    private void broadcastTransferUpdate() {
        messagingTemplate.convertAndSend("/topic/transfers", getAllTransfers());
    }
}
