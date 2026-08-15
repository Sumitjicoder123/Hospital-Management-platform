package com.hospital.platform.service;

import com.hospital.platform.config.JwtUtils;
import com.hospital.platform.dto.AuthDTOs.*;
import com.hospital.platform.entity.User;
import com.hospital.platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials: User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials: Password mismatch");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getHospitalId());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole().name(), user.getHospitalId(), user.getDoctorId());
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

        userRepository.save(user);
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getHospitalId());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole().name(), user.getHospitalId(), user.getDoctorId());
    }

    public AuthResponse getCurrentUser(String token) {
        if (!jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        String email = jwtUtils.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole().name(), user.getHospitalId(), user.getDoctorId());
    }
}
