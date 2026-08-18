package com.hospital.platform.service;

import com.hospital.platform.config.JwtUtils;
import com.hospital.platform.dto.AuthDTOs.*;
import com.hospital.platform.entity.User;
import com.hospital.platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hospital.platform.entity.Doctor;
import com.hospital.platform.repository.DoctorRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final DoctorRepository doctorRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.doctorRepository = doctorRepository;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials: User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials: Password mismatch");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getHospitalId());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole().name(), user.getHospitalId(), user.getDoctorId(), user.isProfileCompleted(), user.getProfilePicture());
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already registered!");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Error: Phone number is already registered to another account!");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                request.getRole(),
                request.getHospitalId(),
                request.getDoctorId()
        );
        user.setProfileCompleted(true); // Manually registered users are completed by default
        
        userRepository.save(user);

        if (request.getRole() == com.hospital.platform.entity.Role.DOCTOR) {
            Doctor doctor = new Doctor(
                user.getId(),
                request.getHospitalId(),
                request.getDepartmentId(),
                request.getName(),
                "General Practice", // Default specialization
                Doctor.DoctorStatus.AVAILABLE,
                15
            );
            doctor = doctorRepository.save(doctor);
            user.setDoctorId(doctor.getId());
            userRepository.save(user);
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getHospitalId());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole().name(), user.getHospitalId(), user.getDoctorId(), user.isProfileCompleted(), user.getProfilePicture());
    }

    @org.springframework.beans.factory.annotation.Value("${supabase.url}")
    private String supabaseUrl;

    @org.springframework.beans.factory.annotation.Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    public AuthResponse getCurrentUser(String token) {
        String email = null;
        String googleId = null;
        String name = null;
        String avatarUrl = null;

        if (jwtUtils.validateToken(token)) {
            email = jwtUtils.getEmailFromToken(token);
        } else {
            // Validate via Supabase API if local signature check fails (e.g., ECC keys)
            try {
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setBearerAuth(token);
                headers.set("apikey", supabaseAnonKey);
                org.springframework.http.HttpEntity<Void> request = new org.springframework.http.HttpEntity<>(headers);
                org.springframework.http.ResponseEntity<java.util.Map> response = restTemplate.exchange(
                    supabaseUrl + "/auth/v1/user", 
                    org.springframework.http.HttpMethod.GET, 
                    request, 
                    java.util.Map.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    java.util.Map<String, Object> body = response.getBody();
                    email = (String) body.get("email");
                    googleId = (String) body.get("id");
                    
                    if (body.get("user_metadata") != null) {
                        java.util.Map<String, Object> meta = (java.util.Map<String, Object>) body.get("user_metadata");
                        if (meta.get("full_name") != null) {
                            name = (String) meta.get("full_name");
                        }
                        if (meta.get("avatar_url") != null) {
                            avatarUrl = (String) meta.get("avatar_url");
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore, will throw invalid token below
            }
        }

        if (email == null) {
            throw new RuntimeException("Invalid token");
        }

        final String finalEmail = email;
        final String finalGoogleId = googleId;
        final String finalName = name;
        final String finalAvatar = avatarUrl;
        
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            if (finalGoogleId == null) {
                throw new RuntimeException("User not found in local database");
            }
            // Auto-create user from Supabase Google Auth login
            User newUser = new User();
            newUser.setEmail(finalEmail);
            newUser.setGoogleId(finalGoogleId);
            newUser.setPassword("google-auth-placeholder");
            newUser.setName(finalName != null ? finalName : "New Patient");
            newUser.setProfilePicture(finalAvatar);
            newUser.setRole(com.hospital.platform.entity.Role.PATIENT);
            newUser.setProfileCompleted(false);
            return userRepository.save(newUser);
        });
        
        // Ensure googleId is set if they previously registered without one
        if (googleId != null && user.getGoogleId() == null) {
            user.setGoogleId(googleId);
            if (avatarUrl != null) user.setProfilePicture(avatarUrl);
            userRepository.save(user);
        }
        
        // Always issue a fresh local token to speed up future requests
        String localToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getHospitalId());
        
        return new AuthResponse(localToken, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole().name(), user.getHospitalId(), user.getDoctorId(), user.isProfileCompleted(), user.getProfilePicture());
    }

    public AuthResponse completeProfile(String token, CompleteProfileRequest request) {
        AuthResponse currentUserResponse = getCurrentUser(token);
        User user = userRepository.findByEmail(currentUserResponse.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setProfileCompleted(true);

        userRepository.save(user);
        
        String localToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getHospitalId());
        return new AuthResponse(localToken, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole().name(), user.getHospitalId(), user.getDoctorId(), user.isProfileCompleted(), user.getProfilePicture());
    }
}
