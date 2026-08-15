package com.hospital.platform.dto;

import com.hospital.platform.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthDTOs {

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        private String email;
        
        @NotBlank(message = "Password is required")
        private String password;

        public LoginRequest() {}
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", message = "Password must be at least 8 characters long and contain at least one letter and one number")
        private String password;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
        private String phone;

        private Role role = Role.PATIENT;
        private Long hospitalId;
        private Long doctorId; // optional: links a DOCTOR-role registration to an existing Doctor record

        public RegisterRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }

        public Long getHospitalId() { return hospitalId; }
        public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    }

    public static class AuthResponse {
        private String token;
        private Long userId;
        private String name;
        private String email;
        private String phone;
        private String role;
        private Long hospitalId;
        private Long doctorId;

        public AuthResponse() {}
        public AuthResponse(String token, Long userId, String name, String email, String phone, String role, Long hospitalId, Long doctorId) {
            this.token = token;
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.hospitalId = hospitalId;
            this.doctorId = doctorId;
        }

        public String getToken() { return token; }
        public Long getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getRole() { return role; }
        public Long getHospitalId() { return hospitalId; }
        public Long getDoctorId() { return doctorId; }
    }
}
