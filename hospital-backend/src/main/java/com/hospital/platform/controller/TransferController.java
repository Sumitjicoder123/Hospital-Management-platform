package com.hospital.platform.controller;

import com.hospital.platform.dto.OperationDTOs.HospitalMatchScoreDTO;
import com.hospital.platform.entity.TransferRequest;
import com.hospital.platform.entity.Ward;
import com.hospital.platform.service.RecommendationService;
import com.hospital.platform.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;
    private final RecommendationService recommendationService;

    public TransferController(TransferService transferService, RecommendationService recommendationService) {
        this.transferService = transferService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<TransferRequest>> getAllTransfers() {
        return ResponseEntity.ok(transferService.getAllTransfers());
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<TransferRequest>> getTransfersForHospital(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(transferService.getTransfersForHospital(hospitalId));
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<HospitalMatchScoreDTO>> getRecommendations(@RequestParam Long sourceHospitalId,
                                                                          @RequestParam Ward.BedType requiredBedType) {
        return ResponseEntity.ok(recommendationService.findAndRankHospitals(sourceHospitalId, requiredBedType));
    }

    @PostMapping("/initiate")
    public ResponseEntity<TransferRequest> initiateTransfer(@RequestParam(required = false) Long patientId,
                                                            @RequestParam String patientName,
                                                            @RequestParam Long sourceHospitalId,
                                                            @RequestParam Long destinationHospitalId,
                                                            @RequestParam Ward.BedType requiredBedType,
                                                            @RequestParam(defaultValue = "HIGH") String urgency,
                                                            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(transferService.initiateTransfer(patientId, patientName, sourceHospitalId, destinationHospitalId, requiredBedType, urgency, notes));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<TransferRequest> acceptTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.acceptTransfer(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<TransferRequest> rejectTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.rejectTransfer(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TransferRequest> updateStatus(@PathVariable Long id, @RequestParam TransferRequest.TransferStatus status) {
        return ResponseEntity.ok(transferService.updateTransferStatus(id, status));
    }
}
