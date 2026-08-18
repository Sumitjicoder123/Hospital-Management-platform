package com.hospital.platform.controller;

import com.hospital.platform.dto.AuthDTOs.RegisterRequest;
import com.hospital.platform.entity.Doctor;
import com.hospital.platform.entity.User;
import com.hospital.platform.entity.Role;
import com.hospital.platform.repository.DoctorRepository;
import com.hospital.platform.repository.UserRepository;
import com.hospital.platform.service.SupabaseAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SupabaseAdminService supabaseAdminService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public AdminController(SupabaseAdminService supabaseAdminService, UserRepository userRepository, DoctorRepository doctorRepository) {
        this.supabaseAdminService = supabaseAdminService;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    public static class OnboardDoctorRequest {
        private String name;
        private String email;
        private String phone;
        private Long hospitalId;
        private Long departmentId;
        private String specialization;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Long getHospitalId() { return hospitalId; }
        public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }
    }

    @PostMapping("/doctors/register")
    public ResponseEntity<?> registerDoctor(@Valid @RequestBody OnboardDoctorRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        try {
            // 1. Create the account in Supabase
            String tempPassword = supabaseAdminService.createDoctorAuthAccount(req.getEmail());

            // 2. Create the User in our local DB
            User newUser = new User(req.getName(), req.getEmail(), "supabase-managed", req.getPhone(), Role.DOCTOR, req.getHospitalId(), null);
            User savedUser = userRepository.save(newUser);

            // 3. Create the Doctor profile
            Doctor newDoctor = new Doctor(
                    savedUser.getId(),
                    req.getHospitalId(),
                    req.getDepartmentId(),
                    req.getName(),
                    req.getSpecialization() != null ? req.getSpecialization() : "General",
                    Doctor.DoctorStatus.AVAILABLE,
                    15 // default 15 min wait time
            );
            Doctor savedDoctor = doctorRepository.save(newDoctor);

            // Update user with doctorId
            savedUser.setDoctorId(savedDoctor.getId());
            userRepository.save(savedUser);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Doctor created successfully.");
            response.put("email", req.getEmail());
            response.put("temporaryPassword", tempPassword);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to register doctor: " + e.getMessage());
        }
    }
}
