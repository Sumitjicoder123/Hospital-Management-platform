package com.hospital.platform.controller;

import com.hospital.platform.entity.Admission;
import com.hospital.platform.entity.Ward;
import com.hospital.platform.service.AdmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {

    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<Admission>> getAdmissions(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(admissionService.getAdmissionsByHospital(hospitalId));
    }

    @PostMapping("/request")
    public ResponseEntity<Admission> requestAdmission(@RequestParam(required = false) Long patientId,
                                                       @RequestParam String patientName,
                                                       @RequestParam Long hospitalId,
                                                       @RequestParam Long doctorId,
                                                       @RequestParam Ward.BedType requiredBedType) {
        return ResponseEntity.ok(admissionService.requestAdmission(patientId, patientName, hospitalId, doctorId, requiredBedType));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Admission> confirmAdmission(@PathVariable Long id) {
        return ResponseEntity.ok(admissionService.confirmAdmission(id));
    }

    @PutMapping("/{id}/discharge")
    public ResponseEntity<Admission> dischargePatient(@PathVariable Long id) {
        return ResponseEntity.ok(admissionService.dischargePatient(id));
    }
}
