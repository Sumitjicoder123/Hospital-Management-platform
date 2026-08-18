package com.hospital.platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseAdminService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    private final RestTemplate restTemplate;

    public SupabaseAdminService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Creates a new user in Supabase Auth via the Admin API.
     * Generates a temporary password for the doctor.
     * Returns the generated password and the Supabase UUID (if needed).
     */
    public String createDoctorAuthAccount(String email) {
        String url = supabaseUrl + "/auth/v1/admin/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseServiceRoleKey);
        headers.setBearerAuth(supabaseServiceRoleKey);

        String tempPassword = "TempPassword123!"; // Real system would generate a random one

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", tempPassword);
        body.put("email_confirm", true); // Auto-confirm for staff

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            // We successfully created the user in Supabase.
            return tempPassword;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create doctor account in Supabase: " + e.getMessage());
        }
    }
}
