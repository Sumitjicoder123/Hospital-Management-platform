package com.hospital.platform.service;

import com.hospital.platform.entity.Patient;
import com.hospital.platform.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
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

        return patientRepository.findByPhone(phone)
                .map(existing -> {
                    // Keep the name reasonably fresh if it was blank/placeholder before.
                    if (name != null && !name.isBlank() && !name.equals(existing.getName())) {
                        existing.setName(name);
                        patientRepository.save(existing);
                    }
                    return existing;
                })
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
