package com.hospital.platform.config;

import com.hospital.platform.entity.Doctor;
import com.hospital.platform.entity.DoctorAvailability;
import com.hospital.platform.repository.DoctorAvailabilityRepository;
import com.hospital.platform.repository.DoctorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Component
public class DoctorAvailabilitySeeder implements CommandLineRunner {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    public DoctorAvailabilitySeeder(DoctorRepository doctorRepository, DoctorAvailabilityRepository availabilityRepository) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (availabilityRepository.count() > 0) {
            System.out.println("Doctor availability already seeded — skipping initialization.");
            return;
        }

        System.out.println("Seeding doctor availability data...");

        List<Doctor> doctors = doctorRepository.findAll();
        for (Doctor doctor : doctors) {
            // Give every doctor M-F 09:00-13:00 and 15:00-19:00, 15m slots
            for (int i = 1; i <= 5; i++) {
                DayOfWeek day = DayOfWeek.of(i);
                availabilityRepository.save(new DoctorAvailability(doctor.getId(), day, LocalTime.of(9, 0), LocalTime.of(13, 0), 15));
                availabilityRepository.save(new DoctorAvailability(doctor.getId(), day, LocalTime.of(15, 0), LocalTime.of(19, 0), 15));
            }
        }
    }
}
