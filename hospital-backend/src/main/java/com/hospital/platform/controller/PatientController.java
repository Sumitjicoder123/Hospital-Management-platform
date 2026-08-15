package com.hospital.platform.controller;

import com.hospital.platform.entity.Patient;
import com.hospital.platform.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Patient> getByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(patientService.getByPhone(phone));
    }
}
