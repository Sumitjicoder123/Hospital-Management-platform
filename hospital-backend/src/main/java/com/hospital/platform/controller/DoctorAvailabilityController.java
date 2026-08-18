package com.hospital.platform.controller;

import com.hospital.platform.dto.OperationDTOs.AvailableSlotResponse;
import com.hospital.platform.dto.OperationDTOs.DoctorAvailabilityRequest;
import com.hospital.platform.entity.DoctorAvailability;
import com.hospital.platform.service.DoctorAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors/{doctorId}")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PutMapping("/availability")
    public ResponseEntity<?> saveAvailability(@PathVariable Long doctorId, @RequestBody List<DoctorAvailabilityRequest> requests) {
        availabilityService.saveAvailability(doctorId, requests);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/availability")
    public ResponseEntity<List<DoctorAvailability>> getAvailability(@PathVariable Long doctorId) {
        return ResponseEntity.ok(availabilityService.getAvailability(doctorId));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<AvailableSlotResponse>> getSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(availabilityService.getAvailableSlots(doctorId, from, to));
    }
    @GetMapping("/availability-summary")
    public ResponseEntity<List<com.hospital.platform.dto.OperationDTOs.AvailabilitySummaryResponse>> getAvailabilitySummary(
            @PathVariable Long doctorId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(availabilityService.getAvailabilitySummary(doctorId, month, year));
    }
}
