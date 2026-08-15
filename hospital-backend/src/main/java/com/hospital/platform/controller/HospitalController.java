package com.hospital.platform.controller;

import com.hospital.platform.entity.*;
import com.hospital.platform.service.HospitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getHospitalById(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getHospitalById(id));
    }

    @GetMapping("/{id}/departments")
    public ResponseEntity<List<Department>> getDepartments(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getDepartmentsByHospital(id));
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<List<Doctor>> getDoctors(@PathVariable Long id, @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(hospitalService.getDoctorsByHospitalAndDepartment(id, departmentId));
    }
}
