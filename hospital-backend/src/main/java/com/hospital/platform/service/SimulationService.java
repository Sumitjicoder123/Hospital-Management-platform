package com.hospital.platform.service;

import com.hospital.platform.dto.OperationDTOs.*;
import com.hospital.platform.entity.Hospital;
import com.hospital.platform.entity.Ward;
import com.hospital.platform.repository.BedRepository;
import com.hospital.platform.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationService {

    private final HospitalRepository hospitalRepository;
    private final BedRepository bedRepository;

    public SimulationService(HospitalRepository hospitalRepository, BedRepository bedRepository) {
        this.hospitalRepository = hospitalRepository;
        this.bedRepository = bedRepository;
    }

    public WhatIfSimulationResponse runSimulation(WhatIfSimulationRequest req) {
        List<Hospital> hospitals = hospitalRepository.findAll();
        
        int totalCurrentAvailableIcu = 0;
        List<RedistributionItem> redistribution = new ArrayList<>();

        for (Hospital h : hospitals) {
            long availIcu = bedRepository.countAvailableBedsByType(h.getId(), Ward.BedType.ICU);
            totalCurrentAvailableIcu += (int) availIcu;
        }

        int demand = req.getAdditionalIcuPatients() != null ? req.getAdditionalIcuPatients() : 30;
        int shortage = Math.max(0, demand - totalCurrentAvailableIcu);

        // Distribute incoming patients across hospitals proportional to their available capacity
        for (Hospital h : hospitals) {
            long availIcu = bedRepository.countAvailableBedsByType(h.getId(), Ward.BedType.ICU);
            int recommended = 0;
            if (totalCurrentAvailableIcu > 0) {
                recommended = (int) Math.round(((double) availIcu / totalCurrentAvailableIcu) * Math.min(demand, totalCurrentAvailableIcu));
            }
            redistribution.add(new RedistributionItem(h.getName(), h.getId(), (int) availIcu, recommended));
        }

        String severity = shortage == 0 ? "MANAGEABLE" : (shortage > 15 ? "CRITICAL_SHORTAGE" : "MODERATE_SHORTAGE");

        return new WhatIfSimulationResponse(
                totalCurrentAvailableIcu,
                demand,
                shortage,
                severity,
                redistribution
        );
    }
}
