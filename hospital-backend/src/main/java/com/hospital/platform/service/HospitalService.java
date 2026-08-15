package com.hospital.platform.service;

import com.hospital.platform.entity.*;
import com.hospital.platform.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public HospitalService(HospitalRepository hospitalRepository,
                           DepartmentRepository departmentRepository,
                           DoctorRepository doctorRepository,
                           BedRepository bedRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.hospitalRepository = hospitalRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.bedRepository = bedRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Hospital> getAllHospitals() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        hospitals.forEach(this::updateHospitalCapacityStats);
        return hospitals;
    }

    public Hospital getHospitalById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found: " + id));
        updateHospitalCapacityStats(hospital);
        return hospital;
    }

    public List<Department> getDepartmentsByHospital(Long hospitalId) {
        return departmentRepository.findByHospitalId(hospitalId);
    }

    public List<Doctor> getDoctorsByHospitalAndDepartment(Long hospitalId, Long departmentId) {
        if (departmentId != null) {
            return doctorRepository.findByHospitalIdAndDepartmentId(hospitalId, departmentId);
        }
        return doctorRepository.findByHospitalId(hospitalId);
    }

    @Transactional
    public void updateHospitalCapacityStats(Hospital hospital) {
        List<Bed> beds = bedRepository.findByHospitalId(hospital.getId());
        
        int total = beds.size();
        int occupied = (int) beds.stream().filter(b -> b.getStatus() == Bed.BedStatus.OCCUPIED || b.getStatus() == Bed.BedStatus.RESERVED).count();
        int available = (int) beds.stream().filter(b -> b.getStatus() == Bed.BedStatus.AVAILABLE).count();

        int icuTotal = (int) beds.stream().filter(b -> b.getBedType() == Ward.BedType.ICU).count();
        int icuOccupied = (int) beds.stream().filter(b -> b.getBedType() == Ward.BedType.ICU && (b.getStatus() == Bed.BedStatus.OCCUPIED || b.getStatus() == Bed.BedStatus.RESERVED)).count();
        int icuAvailable = (int) beds.stream().filter(b -> b.getBedType() == Ward.BedType.ICU && b.getStatus() == Bed.BedStatus.AVAILABLE).count();

        int emergencyTotal = (int) beds.stream().filter(b -> b.getBedType() == Ward.BedType.EMERGENCY).count();
        int emergencyOccupied = (int) beds.stream().filter(b -> b.getBedType() == Ward.BedType.EMERGENCY && (b.getStatus() == Bed.BedStatus.OCCUPIED || b.getStatus() == Bed.BedStatus.RESERVED)).count();
        int emergencyAvailable = (int) beds.stream().filter(b -> b.getBedType() == Ward.BedType.EMERGENCY && b.getStatus() == Bed.BedStatus.AVAILABLE).count();

        hospital.setTotalBeds(total);
        hospital.setOccupiedBeds(occupied);
        hospital.setAvailableBeds(available);

        hospital.setTotalIcuBeds(icuTotal);
        hospital.setOccupiedIcuBeds(icuOccupied);
        hospital.setAvailableIcuBeds(icuAvailable);

        hospital.setTotalEmergencyBeds(emergencyTotal);
        hospital.setOccupiedEmergencyBeds(emergencyOccupied);
        hospital.setAvailableEmergencyBeds(emergencyAvailable);

        // PRD Section 22: Hospital Overload Detection (ICU > 90% or Total > 85%)
        double occupancyRate = total > 0 ? (double) occupied / total : 0;
        double icuOccupancyRate = icuTotal > 0 ? (double) icuOccupied / icuTotal : 0;

        if (occupancyRate > 0.85 || icuOccupancyRate > 0.90) {
            hospital.setStatus(Hospital.HospitalStatus.CRITICAL);
        } else if (occupancyRate > 0.70 || icuOccupancyRate > 0.75) {
            hospital.setStatus(Hospital.HospitalStatus.HIGH_OCCUPANCY);
        } else {
            hospital.setStatus(Hospital.HospitalStatus.NORMAL);
        }

        hospitalRepository.save(hospital);
    }
}
