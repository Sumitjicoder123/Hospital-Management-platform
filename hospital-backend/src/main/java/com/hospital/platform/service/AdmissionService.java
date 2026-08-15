package com.hospital.platform.service;

import com.hospital.platform.entity.*;
import com.hospital.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final BedRepository bedRepository;
    private final BedService bedService;

    public AdmissionService(AdmissionRepository admissionRepository,
                            BedRepository bedRepository,
                            BedService bedService) {
        this.admissionRepository = admissionRepository;
        this.bedRepository = bedRepository;
        this.bedService = bedService;
    }

    public List<Admission> getAdmissionsByHospital(Long hospitalId) {
        return admissionRepository.findByHospitalId(hospitalId);
    }

    @Transactional
    public Admission requestAdmission(Long patientId, String patientName, Long hospitalId, Long doctorId, Ward.BedType requiredBedType) {
        Admission admission = new Admission(patientId, patientName, hospitalId, doctorId, requiredBedType);
        
        // Check for available bed of required type
        List<Bed> availableBeds = bedRepository.findByHospitalIdAndBedTypeAndStatus(hospitalId, requiredBedType, Bed.BedStatus.AVAILABLE);

        if (!availableBeds.isEmpty()) {
            Bed selectedBed = availableBeds.get(0);
            bedService.reserveBed(selectedBed.getId(), patientId, patientName);
            admission.setBedId(selectedBed.getId());
            admission.setStatus(Admission.AdmissionStatus.RESERVED);
        } else {
            admission.setStatus(Admission.AdmissionStatus.REQUESTED);
        }

        return admissionRepository.save(admission);
    }

    @Transactional
    public Admission confirmAdmission(Long admissionId) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new RuntimeException("Admission not found: " + admissionId));

        if (admission.getBedId() != null) {
            bedService.updateBedStatus(admission.getBedId(), Bed.BedStatus.OCCUPIED);
        }
        admission.setStatus(Admission.AdmissionStatus.ADMITTED);
        admission.setAdmittedTime(LocalDateTime.now());
        return admissionRepository.save(admission);
    }

    @Transactional
    public Admission dischargePatient(Long admissionId) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new RuntimeException("Admission not found: " + admissionId));

        if (admission.getBedId() != null) {
            bedService.updateBedStatus(admission.getBedId(), Bed.BedStatus.CLEANING);
        }
        admission.setStatus(Admission.AdmissionStatus.DISCHARGED);
        admission.setDischargedTime(LocalDateTime.now());
        return admissionRepository.save(admission);
    }
}
