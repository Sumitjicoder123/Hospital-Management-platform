package com.hospital.platform.controller;

import com.hospital.platform.dto.OperationDTOs.MedicalRecordRequest;
import com.hospital.platform.entity.MedicalRecord;
import com.hospital.platform.service.MedicalRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    // Doctor saves diagnosis/prescription/vitals when finishing a consultation.
    @PostMapping
    public ResponseEntity<MedicalRecord> createRecord(@RequestBody MedicalRecordRequest request) {
        return ResponseEntity.ok(medicalRecordService.createRecord(request));
    }

    // Doctor "Patient Snapshot" panel: full visit history before/during consultation.
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecord>> getByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getHistoryByPatientId(patientId));
    }

    // Fallback for walk-in / WhatsApp-only patients with no account.
    @GetMapping("/phone/{phone}")
    public ResponseEntity<List<MedicalRecord>> getByPhone(@PathVariable String phone, @RequestParam(required = false) String name) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(medicalRecordService.getHistoryByPhoneAndName(phone, name));
        }
        return ResponseEntity.ok(medicalRecordService.getHistoryByPhone(phone));
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<MedicalRecord>> getByHospital(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(medicalRecordService.getHospitalRecords(hospitalId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecord> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.getById(id));
    }

    @ExceptionHandler(java.time.format.DateTimeParseException.class)
    public ResponseEntity<String> handleDateTimeParseException(java.time.format.DateTimeParseException ex) {
        return ResponseEntity.badRequest().body("Invalid date format for follow-up date. Please use YYYY-MM-DD.");
    }
}
