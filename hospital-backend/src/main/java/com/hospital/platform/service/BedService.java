package com.hospital.platform.service;

import com.hospital.platform.entity.*;
import com.hospital.platform.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BedService {

    private final BedRepository bedRepository;
    private final WardRepository wardRepository;
    private final HospitalService hospitalService;
    private final SimpMessagingTemplate messagingTemplate;

    public BedService(BedRepository bedRepository,
                      WardRepository wardRepository,
                      HospitalService hospitalService,
                      SimpMessagingTemplate messagingTemplate) {
        this.bedRepository = bedRepository;
        this.wardRepository = wardRepository;
        this.hospitalService = hospitalService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Bed> getBedsByHospital(Long hospitalId) {
        return bedRepository.findByHospitalId(hospitalId);
    }

    public List<Ward> getWardsByHospital(Long hospitalId) {
        return wardRepository.findByHospitalId(hospitalId);
    }

    @Transactional
    public Bed reserveBed(Long bedId, Long patientId, String patientName) {
        try {
            Bed bed = bedRepository.findById(bedId)
                    .orElseThrow(() -> new RuntimeException("Bed not found: " + bedId));

            if (bed.getStatus() != Bed.BedStatus.AVAILABLE) {
                throw new RuntimeException("Bed " + bed.getBedNumber() + " is no longer available! Status: " + bed.getStatus());
            }

            bed.setStatus(Bed.BedStatus.RESERVED);
            bed.setCurrentPatientId(patientId);
            bed.setCurrentPatientName(patientName);

            Bed updatedBed = bedRepository.save(bed);

            hospitalService.updateHospitalCapacityStats(hospitalService.getHospitalById(bed.getHospitalId()));
            broadcastBedUpdate(bed.getHospitalId());

            return updatedBed;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Concurrency conflict: Bed was just modified by another user. Please try again.");
        }
    }

    @Transactional
    public Bed updateBedStatus(Long bedId, Bed.BedStatus newStatus) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new RuntimeException("Bed not found: " + bedId));

        bed.setStatus(newStatus);
        if (newStatus == Bed.BedStatus.AVAILABLE || newStatus == Bed.BedStatus.CLEANING) {
            bed.setCurrentPatientId(null);
            bed.setCurrentPatientName(null);
        }

        Bed saved = bedRepository.save(bed);
        hospitalService.updateHospitalCapacityStats(hospitalService.getHospitalById(bed.getHospitalId()));
        broadcastBedUpdate(bed.getHospitalId());

        return saved;
    }

    private void broadcastBedUpdate(Long hospitalId) {
        messagingTemplate.convertAndSend("/topic/beds/" + hospitalId, getBedsByHospital(hospitalId));
    }
}
