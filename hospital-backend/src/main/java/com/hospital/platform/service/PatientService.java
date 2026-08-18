package com.hospital.platform.service;

import com.hospital.platform.entity.Patient;
import com.hospital.platform.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final JdbcTemplate jdbcTemplate;

    public PatientService(PatientRepository patientRepository, JdbcTemplate jdbcTemplate) {
        this.patientRepository = patientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void dropUniquePhoneConstraint() {
        try {
            String sql = "DO $$ DECLARE constraint_name text; " +
                    "BEGIN " +
                    "SELECT conname INTO constraint_name " +
                    "FROM pg_constraint " +
                    "WHERE conrelid = 'patients'::regclass " +
                    "AND contype = 'u' " +
                    "AND conkey = (SELECT array_agg(attnum) FROM pg_attribute WHERE attrelid = 'patients'::regclass AND attname = 'phone'); " +
                    "IF constraint_name IS NOT NULL THEN " +
                    "EXECUTE 'ALTER TABLE patients DROP CONSTRAINT ' || constraint_name; " +
                    "END IF; " +
                    "END $$;";
            jdbcTemplate.execute(sql);
            System.out.println("Dropped unique constraint on patients.phone if it existed.");
        } catch (Exception e) {
            System.err.println("Could not drop unique constraint on patients.phone: " + e.getMessage());
        }
    }

    /**
     * Strips everything except digits so "9876543210", "+91 98765 43210" and
     * "98765-43210" are all recognized as the same patient. This is the fix
     * for history "disappearing" when a phone number was typed slightly
     * differently between two visits.
     */
    public static String normalizePhone(String rawPhone) {
        if (rawPhone == null) return null;
        return rawPhone.replaceAll("[^0-9]", "");
    }

    /**
     * Looks up a patient by (normalized) phone number, creating a new record
     * the first time this phone number is ever seen. Every booking - web or
     * WhatsApp - should call this so patientId stays consistent across visits.
     */
    @Transactional
    public Patient findOrCreateByPhone(String name, String rawPhone) {
        String phone = normalizePhone(rawPhone);
        if (phone == null || phone.isBlank()) {
            // No phone to key off - can't reliably link this to a patient record.
            return null;
        }

        if (name == null || name.isBlank()) {
            return patientRepository.findByPhone(phone)
                    .orElseGet(() -> patientRepository.save(new Patient(name, phone)));
        }

        return patientRepository.findByNameIgnoreCaseAndPhone(name, phone)
                .orElseGet(() -> patientRepository.save(new Patient(name, phone)));
    }

    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + id));
    }

    public Patient getByPhone(String rawPhone) {
        String phone = normalizePhone(rawPhone);
        return patientRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("No patient found for phone: " + rawPhone));
    }
}
