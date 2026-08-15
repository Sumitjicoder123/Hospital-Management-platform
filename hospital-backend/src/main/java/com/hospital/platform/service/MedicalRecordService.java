package com.hospital.platform.service;

import com.hospital.platform.dto.OperationDTOs.MedicalRecordRequest;
import com.hospital.platform.entity.MedicalRecord;
import com.hospital.platform.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientService patientService;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository, PatientService patientService) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientService = patientService;
    }

    @Transactional
    public MedicalRecord createRecord(MedicalRecordRequest req) {
        Long patientId = req.getPatientId();
        if (patientId == null && req.getPatientPhone() != null && !req.getPatientPhone().isBlank()) {
            // Same phone-based recognition used at booking time, so a visit
            // recorded here always lands under the correct patient even if
            // the caller didn't already know the patientId.
            var patient = patientService.findOrCreateByPhone(req.getPatientName(), req.getPatientPhone());
            if (patient != null) patientId = patient.getId();
        }

        MedicalRecord record = new MedicalRecord(
                patientId,
                req.getPatientName(),
                req.getPatientPhone(),
                req.getHospitalId(),
                req.getDoctorId(),
                req.getDoctorName(),
                req.getDepartmentId(),
                req.getAppointmentId()
        );
        record.setSymptoms(req.getSymptoms());
        record.setDiagnosis(req.getDiagnosis());
        record.setPrescription(req.getPrescription());
        record.setNotes(req.getNotes());
        record.setBloodPressure(req.getBloodPressure());
        record.setTemperatureCelsius(req.getTemperatureCelsius());
        record.setPulseRate(req.getPulseRate());
        record.setWeightKg(req.getWeightKg());

        if (req.getFollowUpDate() != null && !req.getFollowUpDate().isBlank()) {
            try {
                record.setFollowUpDate(LocalDateTime.parse(req.getFollowUpDate()));
            } catch (Exception ignored) {
                // If the string isn't ISO-8601, just skip it rather than fail the whole save.
            }
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Full visit history for a patient, most recent first. Used by the doctor
     * "patient snapshot" panel before/during a consultation.
     */
    public List<MedicalRecord> getHistoryByPatientId(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId);
    }

    /**
     * Fallback lookup for patients without a registered account (e.g. booked
     * purely through WhatsApp) — phone number is the only stable identifier.
     */
    public List<MedicalRecord> getHistoryByPhone(String phone) {
        return medicalRecordRepository.findByPatientPhoneOrderByVisitDateDesc(phone);
    }

    public List<MedicalRecord> getHospitalRecords(Long hospitalId) {
        return medicalRecordRepository.findByHospitalIdOrderByVisitDateDesc(hospitalId);
    }

    public MedicalRecord getById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found: " + id));
    }
}
