package com.hospital.platform.controller;

import com.hospital.platform.dto.OperationDTOs.BedReserveRequest;
import com.hospital.platform.entity.Bed;
import com.hospital.platform.entity.Ward;
import com.hospital.platform.service.BedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @GetMapping("/hospitals/{hospitalId}/beds")
    public ResponseEntity<List<Bed>> getBeds(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(bedService.getBedsByHospital(hospitalId));
    }

    @GetMapping("/hospitals/{hospitalId}/wards")
    public ResponseEntity<List<Ward>> getWards(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(bedService.getWardsByHospital(hospitalId));
    }

    @PostMapping("/beds/reserve")
    public ResponseEntity<Bed> reserveBed(@RequestBody BedReserveRequest request) {
        return ResponseEntity.ok(bedService.reserveBed(request.getBedId(), request.getPatientId(), request.getPatientName()));
    }

    @PutMapping("/beds/{id}/status")
    public ResponseEntity<Bed> updateBedStatus(@PathVariable Long id, @RequestParam Bed.BedStatus status) {
        return ResponseEntity.ok(bedService.updateBedStatus(id, status));
    }
}
